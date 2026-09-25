// UserSettingsFunction — API Gateway Lambda proxy for per-user video
// generation settings: whether a user rides on the app's shared/enterprise
// Higgsfield key (metered, monthly quota) or has connected their own
// Higgsfield account (BYOK — unmetered by this app, billed to their own
// Higgsfield account instead).
//
// Routes (same Cognito JWT authorizer as /api/generate-video):
//   GET    /api/user-settings               -> current provider + quota usage
//   POST   /api/user-settings/higgsfield-key -> store caller's own key (KMS-encrypted), switch to BYOK
//   DELETE /api/user-settings/higgsfield-key -> forget caller's key, revert to enterprise
//
// The caller's Higgsfield credentials are only ever KMS-encrypted at rest
// and decrypted server-side (in generate-video's Lambda) at generation
// time — this Lambda never returns the stored secret, only a boolean
// "connected" status.
//
// Table shape: USER_SETTINGS_TABLE_NAME, partition key "sub" (String), sort
// key "settingType" (String). Two item kinds share the table:
//   { sub, settingType: "profile", generationProvider, hfCredentialsCiphertext? }
//   { sub, settingType: "usage#YYYY-MM", minutesUsed }  (one fresh item per
//     calendar month — this is what makes monthly reset free: a new month
//     is just an item that doesn't exist yet, no reset job needed)
// generate-video/index.js reads/writes the same two item kinds and must
// stay in sync with this shape if either changes.
//
// Required env vars: USER_SETTINGS_TABLE_NAME, HF_CREDENTIALS_KMS_KEY_ID
// (KMS key with kms:Encrypt granted to this Lambda's role),
// ENTERPRISE_MONTHLY_QUOTA_MINUTES (optional, default 30 — must match the
// same env var on GenerateVideoFunction, which is what actually enforces it).

const { DynamoDBClient } = require("@aws-sdk/client-dynamodb");
const { DynamoDBDocumentClient, GetCommand, PutCommand } = require("@aws-sdk/lib-dynamodb");
const { KMSClient, EncryptCommand } = require("@aws-sdk/client-kms");

const ddb = DynamoDBDocumentClient.from(new DynamoDBClient({}));
const kms = new KMSClient({});

const DEFAULT_QUOTA_MINUTES = Number(process.env.ENTERPRISE_MONTHLY_QUOTA_MINUTES || 30);

function jsonResponse(statusCode, body) {
  return {
    statusCode,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  };
}

function getOwnerSub(event) {
  return event.requestContext?.authorizer?.jwt?.claims?.sub ?? null;
}

// Must match generate-video/index.js's currentPeriod() exactly — both
// Lambdas read/write the same usage#<period> item.
function currentPeriod() {
  const now = new Date();
  return `${now.getUTCFullYear()}-${String(now.getUTCMonth() + 1).padStart(2, "0")}`;
}

async function handleGet(sub) {
  const period = currentPeriod();
  const [{ Item: profile }, { Item: usage }] = await Promise.all([
    ddb.send(new GetCommand({ TableName: process.env.USER_SETTINGS_TABLE_NAME, Key: { sub, settingType: "profile" } })),
    ddb.send(new GetCommand({ TableName: process.env.USER_SETTINGS_TABLE_NAME, Key: { sub, settingType: `usage#${period}` } })),
  ]);

  return jsonResponse(200, {
    generationProvider: profile?.generationProvider === "byok" ? "byok" : "enterprise",
    connected: profile?.generationProvider === "byok" && Boolean(profile?.hfCredentialsCiphertext),
    minutesUsedThisMonth: usage?.minutesUsed || 0,
    minutesQuotaMonthly: DEFAULT_QUOTA_MINUTES,
  });
}

async function handleConnect(sub, event) {
  let body;
  try {
    body = JSON.parse(event.body || "{}");
  } catch {
    return jsonResponse(400, { error: "Invalid JSON body" });
  }

  const { keyId, keySecret } = body;
  if (!keyId || !keySecret) {
    return jsonResponse(400, { error: "keyId and keySecret are required" });
  }

  // Same "<id>:<secret>" shape generate-video already builds its
  // Authorization header from — see higgsfieldAuthHeader() there.
  const plaintext = `${keyId}:${keySecret}`;
  const { CiphertextBlob } = await kms.send(
    new EncryptCommand({
      KeyId: process.env.HF_CREDENTIALS_KMS_KEY_ID,
      Plaintext: Buffer.from(plaintext, "utf-8"),
    })
  );

  await ddb.send(
    new PutCommand({
      TableName: process.env.USER_SETTINGS_TABLE_NAME,
      Item: {
        sub,
        settingType: "profile",
        generationProvider: "byok",
        hfCredentialsCiphertext: Buffer.from(CiphertextBlob).toString("base64"),
        updatedAt: Date.now(),
      },
    })
  );

  return jsonResponse(200, { generationProvider: "byok", connected: true });
}

async function handleDisconnect(sub) {
  // Full replace, not a partial update: this is meant to forget
  // hfCredentialsCiphertext entirely, not just flip a flag next to it.
  await ddb.send(
    new PutCommand({
      TableName: process.env.USER_SETTINGS_TABLE_NAME,
      Item: {
        sub,
        settingType: "profile",
        generationProvider: "enterprise",
        updatedAt: Date.now(),
      },
    })
  );

  return jsonResponse(200, { generationProvider: "enterprise", connected: false });
}

exports.handler = async (event) => {
  const sub = getOwnerSub(event);
  if (!sub) {
    return jsonResponse(401, { error: "Not authenticated" });
  }

  const method = event.requestContext?.http?.method;
  const path = event.requestContext?.http?.path || "";

  try {
    if (method === "GET" && path.endsWith("/user-settings")) {
      return await handleGet(sub);
    }
    if (method === "POST" && path.endsWith("/higgsfield-key")) {
      return await handleConnect(sub, event);
    }
    if (method === "DELETE" && path.endsWith("/higgsfield-key")) {
      return await handleDisconnect(sub);
    }
    return jsonResponse(404, { error: "Not found" });
  } catch (err) {
    console.error("UserSettingsFunction error:", err);
    return jsonResponse(500, { error: err.message || "Internal error" });
  }
};
