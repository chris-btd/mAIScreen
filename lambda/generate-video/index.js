// GenerateVideoFunction — API Gateway (HTTP API) Lambda proxy for the
// Higgsfield Seedance 2.5 API. Keeps HF_API_KEY_ID / HF_API_KEY_SECRET
// server-side; the client never sees them.
//
// Routes (both behind the same Cognito JWT authorizer as /api/play,
// /api/upload-url, /api/submit):
//   POST /api/generate-video            -> start a Seedance job
//   GET  /api/generate-video/{requestId} -> poll job status
//
// Seedance's status_url is opaque per-request and (per its docs) requires
// the same "Key <id>:<secret>" auth the client must never hold — so the
// mapping from requestId -> statusUrl is written to DynamoDB on submit and
// read back (scoped to the calling user) on poll, and the poll route
// proxies the actual status call server-side.
//
// Per-user provider: each user either rides the shared enterprise key
// (metered — see reserveEnterpriseQuota below) or has connected their own
// Higgsfield account via user-settings/index.js (BYOK — unmetered by this
// app). Settings live in USER_SETTINGS_TABLE_NAME, the same table
// user-settings/index.js writes to; the BYOK secret is KMS-encrypted at
// rest there and only ever decrypted here, in memory, per-request.
//
// Required env vars: HF_API_KEY_ID, HF_API_KEY_SECRET, REQUESTS_TABLE_NAME
// (DynamoDB table, partition key "requestId" (String), TTL attribute "ttl"),
// USER_SETTINGS_TABLE_NAME (partition key "sub" (String)),
// ENTERPRISE_MONTHLY_QUOTA_MINUTES (optional, default 30 — must match the
// same env var on UserSettingsFunction so both Lambdas agree on quota).
// KMS decrypt permission on the same key user-settings/index.js encrypts
// with is required for the BYOK path (kms:Decrypt on HF_CREDENTIALS_KMS_KEY_ID).

const { DynamoDBClient } = require("@aws-sdk/client-dynamodb");
const { DynamoDBDocumentClient, PutCommand, GetCommand, UpdateCommand } = require("@aws-sdk/lib-dynamodb");
const { KMSClient, DecryptCommand } = require("@aws-sdk/client-kms");

const ddb = DynamoDBDocumentClient.from(new DynamoDBClient({}));
const kms = new KMSClient({});

const HF_BASE_URL = "https://api.higgsfield.ai";
const ALLOWED_WORKFLOWS = new Set([
  "text-to-video",
  "image-to-video",
  "reference-to-video",
  "video-edit",
  "video-extend",
]);
const REQUEST_TTL_SECONDS = 60 * 60 * 24; // 24h, plenty for a client to finish polling
const DEFAULT_QUOTA_MINUTES = Number(process.env.ENTERPRISE_MONTHLY_QUOTA_MINUTES || 30);

function jsonResponse(statusCode, body) {
  return {
    statusCode,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  };
}

// credentials, if given, is a plaintext "<id>:<secret>" string (the
// caller's own decrypted BYOK key). Falls back to the shared enterprise key.
function higgsfieldAuthHeader(credentials) {
  if (credentials) {
    return `Key ${credentials}`;
  }
  const keyId = process.env.HF_API_KEY_ID;
  const keySecret = process.env.HF_API_KEY_SECRET;
  if (!keyId || !keySecret) {
    throw new Error("Missing HF_API_KEY_ID / HF_API_KEY_SECRET environment variables");
  }
  return `Key ${keyId}:${keySecret}`;
}

function getOwnerSub(event) {
  return event.requestContext?.authorizer?.jwt?.claims?.sub ?? null;
}

// "YYYY-MM" in UTC — must match user-settings/index.js's currentPeriod()
// exactly, since both Lambdas read/write the same settings row.
function currentPeriod() {
  const now = new Date();
  return `${now.getUTCFullYear()}-${String(now.getUTCMonth() + 1).padStart(2, "0")}`;
}

async function decryptOwnKey(ciphertextBase64) {
  const { Plaintext } = await kms.send(
    new DecryptCommand({ CiphertextBlob: Buffer.from(ciphertextBase64, "base64") })
  );
  return Buffer.from(Plaintext).toString("utf-8");
}

async function getProfile(ownerSub) {
  const { Item } = await ddb.send(
    new GetCommand({ TableName: process.env.USER_SETTINGS_TABLE_NAME, Key: { sub: ownerSub, settingType: "profile" } })
  );
  return Item || null;
}

// Atomically reserves quota for a new submission — a single conditional
// DynamoDB update, not read-then-write, so concurrent submissions from the
// same user can't race past the monthly limit. Each calendar month is its
// own item (settingType: "usage#YYYY-MM"), so "reset monthly" falls out for
// free: a new month's item simply doesn't exist yet, no cron/reset job
// needed. Returns false (reservation refused) without throwing so the
// caller can surface a clean 402 rather than a generic 500.
async function reserveEnterpriseQuota(ownerSub, requestedMinutes) {
  const period = currentPeriod();
  try {
    await ddb.send(
      new UpdateCommand({
        TableName: process.env.USER_SETTINGS_TABLE_NAME,
        Key: { sub: ownerSub, settingType: `usage#${period}` },
        UpdateExpression: "SET minutesUsed = if_not_exists(minutesUsed, :zero) + :requested, updatedAt = :now",
        ConditionExpression: "attribute_not_exists(minutesUsed) OR minutesUsed <= :maxPriorUsage",
        ExpressionAttributeValues: {
          ":zero": 0,
          ":requested": requestedMinutes,
          ":now": Date.now(),
          // minutesUsed (before this request) must leave room for it —
          // equivalent to (minutesUsed + requested) <= quota, without
          // needing quota itself as an ExpressionAttributeValue twice.
          ":maxPriorUsage": DEFAULT_QUOTA_MINUTES - requestedMinutes,
        },
      })
    );
    return true;
  } catch (err) {
    if (err.name === "ConditionalCheckFailedException") {
      return false;
    }
    throw err;
  }
}

// Best-effort compensation if Higgsfield rejects a submission after quota
// was already reserved for it — not perfectly transactional, but this is an
// internal usage counter, not a payments ledger, so a rare missed rollback
// (e.g. this Lambda crashing mid-request) just self-corrects next month.
async function releaseEnterpriseQuota(ownerSub, requestedMinutes) {
  const period = currentPeriod();
  try {
    await ddb.send(
      new UpdateCommand({
        TableName: process.env.USER_SETTINGS_TABLE_NAME,
        Key: { sub: ownerSub, settingType: `usage#${period}` },
        UpdateExpression: "SET minutesUsed = minutesUsed - :requested, updatedAt = :now",
        ExpressionAttributeValues: { ":requested": requestedMinutes, ":now": Date.now() },
      })
    );
  } catch (err) {
    console.error("Failed to release reserved quota (non-fatal):", err.message || err);
  }
}

// Decides which Higgsfield key a request/poll should use. BYOK bypasses
// quota entirely (their account, their bill). Enterprise/shared path is
// only quota-checked when submitting (requestedDurationSeconds given) —
// polling an already-approved job never re-checks or re-reserves quota.
async function resolveAuth(ownerSub, { requestedDurationSeconds } = {}) {
  const profile = await getProfile(ownerSub);

  if (profile?.generationProvider === "byok" && profile.hfCredentialsCiphertext) {
    const plaintextCreds = await decryptOwnKey(profile.hfCredentialsCiphertext);
    return { provider: "byok", authHeader: higgsfieldAuthHeader(plaintextCreds) };
  }

  if (requestedDurationSeconds !== undefined) {
    const requestedMinutes = requestedDurationSeconds / 60;
    const reserved = await reserveEnterpriseQuota(ownerSub, requestedMinutes);
    if (!reserved) {
      const err = new Error(
        "Monthly generation limit reached. Connect your own Higgsfield account in Settings to keep generating, or wait for your quota to renew."
      );
      err.statusCode = 402;
      throw err;
    }
    return { provider: "enterprise", authHeader: higgsfieldAuthHeader(), reservedMinutes: requestedMinutes };
  }

  return { provider: "enterprise", authHeader: higgsfieldAuthHeader() };
}

// Client sends camelCase; Higgsfield expects snake_case.
function toHiggsfieldPayload(body) {
  const payload = {};
  if (body.prompt !== undefined) payload.prompt = body.prompt;
  if (body.duration !== undefined) payload.duration = body.duration;
  if (body.resolution !== undefined) payload.resolution = body.resolution;
  if (body.aspectRatio !== undefined) payload.aspect_ratio = body.aspectRatio;
  if (body.imageUrl !== undefined) payload.image_url = body.imageUrl;
  if (body.endImageUrl !== undefined) payload.end_image_url = body.endImageUrl;
  if (body.videoUrl !== undefined) payload.video_url = body.videoUrl;
  if (body.imageUrls !== undefined) payload.image_urls = body.imageUrls;
  if (body.videoUrls !== undefined) payload.video_urls = body.videoUrls;
  if (body.audioUrls !== undefined) payload.audio_urls = body.audioUrls;
  return payload;
}

async function handleGenerate(event, ownerSub) {
  let body;
  try {
    body = JSON.parse(event.body || "{}");
  } catch {
    return jsonResponse(400, { error: "Invalid JSON body" });
  }

  const workflow = body.workflow;
  if (!ALLOWED_WORKFLOWS.has(workflow)) {
    return jsonResponse(400, {
      error: `workflow must be one of: ${Array.from(ALLOWED_WORKFLOWS).join(", ")}`,
    });
  }

  // Throws a 402-flagged error (caught below) if this is the enterprise
  // path and the user is out of monthly quota. BYOK skips quota entirely.
  const { provider, authHeader, reservedMinutes } = await resolveAuth(ownerSub, {
    requestedDurationSeconds: body.duration,
  });

  let hfResponse;
  try {
    hfResponse = await fetch(`${HF_BASE_URL}/bytedance/seedance-2.5/${workflow}`, {
      method: "POST",
      headers: {
        Authorization: authHeader,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(toHiggsfieldPayload(body)),
    });
  } catch (err) {
    if (provider === "enterprise" && reservedMinutes) {
      await releaseEnterpriseQuota(ownerSub, reservedMinutes);
    }
    throw err;
  }

  const hfBody = await hfResponse.json();
  if (!hfResponse.ok) {
    if (provider === "enterprise" && reservedMinutes) {
      await releaseEnterpriseQuota(ownerSub, reservedMinutes);
    }
    return jsonResponse(hfResponse.status, { error: hfBody.error || "Higgsfield request failed" });
  }

  const requestId = hfBody.request_id;
  const statusUrl = hfBody.status_url;

  await ddb.send(
    new PutCommand({
      TableName: process.env.REQUESTS_TABLE_NAME,
      Item: {
        requestId,
        statusUrl,
        ownerSub,
        usedProvider: provider, // so handleStatus polls with the same key basis
        createdAt: Date.now(),
        ttl: Math.floor(Date.now() / 1000) + REQUEST_TTL_SECONDS,
      },
    })
  );

  return jsonResponse(200, { requestId, statusUrl });
}

async function handleStatus(requestId, ownerSub) {
  const record = await ddb.send(
    new GetCommand({
      TableName: process.env.REQUESTS_TABLE_NAME,
      Key: { requestId },
    })
  );

  const item = record.Item;
  if (!item || item.ownerSub !== ownerSub) {
    return jsonResponse(404, { error: "Unknown request" });
  }

  // resolveAuth re-derives BYOK vs enterprise from the user's *current*
  // settings each call (never re-checks/re-reserves quota when called this
  // way, with no requestedDurationSeconds). item.usedProvider is kept on
  // the record for observability, not consulted here: if the user
  // disconnected their BYOK key since submitting, this falls back to the
  // enterprise key and surfaces as a clean status-check failure below,
  // rather than silently polling with a key Higgsfield won't recognize for
  // this job — an acceptable, rare edge case rather than one worth more
  // machinery to prevent.
  const { authHeader } = await resolveAuth(ownerSub);

  const hfResponse = await fetch(item.statusUrl, {
    headers: { Authorization: authHeader },
  });
  const hfBody = await hfResponse.json();
  if (!hfResponse.ok) {
    return jsonResponse(hfResponse.status, { error: hfBody.error || "Higgsfield status check failed" });
  }

  return jsonResponse(200, {
    requestId,
    status: hfBody.status,
    videoUrl: hfBody.video?.url ?? hfBody.video_url ?? hfBody.output_url ?? null,
    error: hfBody.error ?? null,
  });
}

exports.handler = async (event) => {
  const ownerSub = getOwnerSub(event);
  if (!ownerSub) {
    return jsonResponse(401, { error: "Not authenticated" });
  }

  const method = event.requestContext?.http?.method;
  const requestId = event.pathParameters?.requestId;

  try {
    if (method === "POST" && !requestId) {
      return await handleGenerate(event, ownerSub);
    }
    if (method === "GET" && requestId) {
      return await handleStatus(requestId, ownerSub);
    }
    return jsonResponse(404, { error: "Not found" });
  } catch (err) {
    console.error("GenerateVideoFunction error:", err);
    return jsonResponse(err.statusCode || 500, { error: err.message || "Internal error" });
  }
};
