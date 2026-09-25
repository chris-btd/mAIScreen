// WebHandoffFunction — mints a short-lived, single-use code the web Creator
// Suite can exchange (via WebHandoffExchangeFunction) for a real Cognito
// session, so opening the web app doesn't require a second login.
//
// Route: POST /api/web-handoff, behind the same Cognito JWT authorizer as
// /api/generate-video. The caller's identity comes from the validated JWT
// claims API Gateway attaches — this function never sees a password.
//
// Required env vars: HANDOFF_TABLE_NAME, WEB_APP_BASE_URL

const crypto = require("crypto");
const { DynamoDBClient } = require("@aws-sdk/client-dynamodb");
const { DynamoDBDocumentClient, PutCommand } = require("@aws-sdk/lib-dynamodb");

const ddb = DynamoDBDocumentClient.from(new DynamoDBClient({}));

const CODE_TTL_SECONDS = 60;

function jsonResponse(statusCode, body) {
  return {
    statusCode,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  };
}

exports.handler = async (event) => {
  const claims = event.requestContext?.authorizer?.jwt?.claims;
  const sub = claims?.sub;
  const username = claims?.["cognito:username"] ?? claims?.username ?? sub;

  if (!sub || !username) {
    return jsonResponse(401, { error: "Not authenticated" });
  }

  const code = crypto.randomBytes(32).toString("hex");
  const nowSeconds = Math.floor(Date.now() / 1000);

  await ddb.send(
    new PutCommand({
      TableName: process.env.HANDOFF_TABLE_NAME,
      Item: {
        code,
        sub,
        username,
        used: false,
        createdAt: Date.now(),
        ttl: nowSeconds + CODE_TTL_SECONDS,
      },
    })
  );

  const webAppBaseUrl = process.env.WEB_APP_BASE_URL;
  return jsonResponse(200, {
    handoffUrl: `${webAppBaseUrl}/auth/handoff?code=${code}`,
  });
};
