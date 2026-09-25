// CreateAuthChallengeFunction — Cognito User Pool trigger for the
// mobile-to-web SSO handoff's CUSTOM_AUTH flow. See
// lambda/web-handoff-exchange/index.js for the overall flow.
//
// The handoff code was already generated and delivered by
// WebHandoffFunction — this trigger has nothing to create. It just leaves
// empty challenge parameters; the real check happens in
// verifyAuthChallengeResponse.js.

exports.handler = async (event) => {
  event.response.publicChallengeParameters = {};
  event.response.privateChallengeParameters = {};
  event.response.challengeMetadata = "WEB_HANDOFF_CODE";
  return event;
};
