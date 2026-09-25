// VerifyAuthChallengeResponseFunction — Cognito User Pool trigger for the
// mobile-to-web SSO handoff's CUSTOM_AUTH flow. See
// lambda/web-handoff-exchange/index.js for the overall flow.
//
// This is the single source of truth for handoff-code validity: checks the
// submitted code exists, belongs to the requesting user, hasn't expired,
// and hasn't already been used — then atomically marks it used so it can
// never be replayed.
//
// Required env vars: HANDOFF_TABLE_NAME

const { DynamoDBClient } = require("@aws-sdk/client-dynamodb");
const { DynamoDBDocumentClient, UpdateCommand } = require("@aws-sdk/lib-dynamodb");

const ddb = DynamoDBDocumentClient.from(new DynamoDBClient({}));

exports.handler = async (event) => {
  const code = event.request.challengeAnswer;
  const username = event.userName;
  const nowSeconds = Math.floor(Date.now() / 1000);

  try {
    const result = await ddb.send(
      new UpdateCommand({
        TableName: process.env.HANDOFF_TABLE_NAME,
        Key: { code },
        UpdateExpression: "SET used = :true",
        ConditionExpression:
          "attribute_exists(code) AND used = :false AND username = :username AND ttl > :now",
        ExpressionAttributeValues: {
          ":true": true,
          ":false": false,
          ":username": username,
          ":now": nowSeconds,
        },
        ReturnValues: "ALL_NEW",
      })
    );
    event.response.answerCorrect = Boolean(result.Attributes);
  } catch (err) {
    // ConditionalCheckFailedException (expired/used/wrong user) lands here —
    // treat any failure as an incorrect answer, never leak details.
    event.response.answerCorrect = false;
  }

  return event;
};
