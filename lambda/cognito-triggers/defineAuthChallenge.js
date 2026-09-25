// DefineAuthChallengeFunction — Cognito User Pool trigger for the
// mobile-to-web SSO handoff's CUSTOM_AUTH flow. See
// lambda/web-handoff-exchange/index.js for the overall flow.
//
// First attempt: request a CUSTOM_CHALLENGE (handled by
// createAuthChallenge.js / verifyAuthChallengeResponse.js).
// After a correct answer: issue tokens. After a wrong answer: fail closed —
// this flow never retries, since handoff codes are single-use.

exports.handler = async (event) => {
  const sessions = event.request.session || [];

  if (sessions.length === 0) {
    event.response.challengeName = "CUSTOM_CHALLENGE";
    event.response.issueTokens = false;
    event.response.failAuthentication = false;
    return event;
  }

  const last = sessions[sessions.length - 1];
  if (last.challengeName === "CUSTOM_CHALLENGE" && last.challengeResult === true) {
    event.response.issueTokens = true;
    event.response.failAuthentication = false;
  } else {
    event.response.issueTokens = false;
    event.response.failAuthentication = true;
  }

  return event;
};
