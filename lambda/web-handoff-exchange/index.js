// WebHandoffExchangeFunction — exchanges a one-time handoff code (minted by
// WebHandoffFunction) for a real Cognito session, via Cognito's CUSTOM_AUTH
// (passwordless) flow. The actual code verification happens inside the
// VerifyAuthChallengeResponseFunction Cognito trigger, not here — this
// function just drives the InitiateAuth/RespondToAuthChallenge handshake.
//
// Route: POST /api/web-handoff/exchange — intentionally NOT behind the JWT
// authorizer (the browser has no session yet). Security comes from the
// code's short TTL, single-use enforcement, and HTTPS-only transport.
//
// Required env vars: HANDOFF_TABLE_NAME, COGNITO_CLIENT_ID
//
// Body: { "code": "<handoff code>" }
//
// CUSTOM_AUTH's InitiateAuth requires a USERNAME, which this function looks
// up from the handoff record (read-only — it does not mark the code used).
// The actual validity check (expired? already used?) happens inside the
// VerifyAuthChallengeResponseFunction Cognito trigger below, which also
// marks the code used, so there's a single source of truth for that state
// even though this function reads the record first to get the username.

const { CognitoIdentityProviderClient, InitiateAuthCommand, RespondToAuthChallengeCommand } = require("@aws-sdk/client-cognito-identity-provider");
const { DynamoDBClient } = require("@aws-sdk/client-dynamodb");
const { DynamoDBDocumentClient, GetCommand } = require("@aws-sdk/lib-dynamodb");

const cognito = new CognitoIdentityProviderClient({});
const ddb = DynamoDBDocumentClient.from(new DynamoDBClient({}));

function jsonResponse(statusCode, body) {
  return {
    statusCode,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  };
}

exports.handler = async (event) => {
  let body;
  try {
    body = JSON.parse(event.body || "{}");
  } catch {
    return jsonResponse(400, { error: "Invalid JSON body" });
  }

  const code = body.code;
  if (!code) {
    return jsonResponse(400, { error: "Missing code" });
  }

  const record = await ddb.send(
    new GetCommand({
      TableName: process.env.HANDOFF_TABLE_NAME,
      Key: { code },
    })
  );

  const item = record.Item;
  if (!item) {
    return jsonResponse(401, { error: "Invalid or expired code" });
  }

  const clientId = process.env.COGNITO_CLIENT_ID;

  try {
    const initiate = await cognito.send(
      new InitiateAuthCommand({
        AuthFlow: "CUSTOM_AUTH",
        ClientId: clientId,
        AuthParameters: { USERNAME: item.username },
      })
    );

    const respond = await cognito.send(
      new RespondToAuthChallengeCommand({
        ClientId: clientId,
        ChallengeName: initiate.ChallengeName,
        Session: initiate.Session,
        ChallengeResponses: {
          USERNAME: item.username,
          ANSWER: code,
        },
      })
    );

    if (!respond.AuthenticationResult) {
      return jsonResponse(401, { error: "Invalid or expired code" });
    }

    return jsonResponse(200, {
      idToken: respond.AuthenticationResult.IdToken,
      accessToken: respond.AuthenticationResult.AccessToken,
      refreshToken: respond.AuthenticationResult.RefreshToken,
    });
  } catch (err) {
    console.error("WebHandoffExchangeFunction error:", err);
    return jsonResponse(401, { error: "Invalid or expired code" });
  }
};
