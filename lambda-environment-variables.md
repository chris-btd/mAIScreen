# Required Lambda Environment Variables for CloudFront Signed Cookies

Your `play` Lambda function needs these environment variables:

## Required Variables:
```
CLOUDFRONT_DOMAIN=d2ofvyi6ramrc2.cloudfront.net
CLOUDFRONT_KEY_PAIR_ID=your-key-pair-id
CLOUDFRONT_PRIVATE_KEY=your-private-key-content
CLOUDFRONT_POLICY_DURATION=3600
```

## Optional Variables:
```
CLOUDFRONT_ACCESS_KEY_ID=your-access-key-id
CLOUDFRONT_SECRET_ACCESS_KEY=your-secret-access-key
```

## CloudFront Configuration Steps:

1. **Create a CloudFront Key Pair:**
   - Go to AWS CloudFront Console
   - Navigate to "Public keys" in the left sidebar
   - Click "Create public key"
   - Upload your public key or generate a new key pair

2. **Create a CloudFront Distribution:**
   - Go to CloudFront Console
   - Create a new distribution
   - Set the origin to your S3 bucket
   - Configure signed URLs/cookies in the "Behaviors" section

3. **Set up the Lambda Function:**
   - Add the environment variables listed above
   - Make sure the Lambda has permissions to access CloudFront
   - Test the function to ensure it generates valid signed cookies

---

# Required Lambda Environment Variables for GenerateVideoFunction (Seedance)

Source: `lambda/generate-video/index.js`. This Lambda proxies the Higgsfield
Seedance 2.5 API so the API key never reaches the client — same pattern as
`play`, `upload-url`, and `submit`.

## Required Variables:
```
HF_API_KEY_ID=your-higgsfield-key-id
HF_API_KEY_SECRET=your-higgsfield-key-secret
REQUESTS_TABLE_NAME=CreatorSuiteRequests
USER_SETTINGS_TABLE_NAME=CreatorSuiteUserSettings
ENTERPRISE_MONTHLY_QUOTA_MINUTES=30
```

## Setup Steps:

1. **Get Higgsfield API credentials** from the Higgsfield dashboard (Key ID + Secret).
2. **Create a DynamoDB table** named `CreatorSuiteRequests` (or your chosen name):
   - Partition key: `requestId` (String)
   - Enable TTL on the `ttl` attribute (Number) — records auto-expire after 24h.
3. **Create the `CreatorSuiteUserSettings` table** (shared with `UserSettingsFunction`
   below — see that section for what it stores):
   - Partition key: `sub` (String)
   - Sort key: `settingType` (String)
   - No TTL needed.
4. **Create the Lambda function** (Node.js 18.x or later runtime — `@aws-sdk/client-dynamodb`,
   `@aws-sdk/lib-dynamodb`, and `@aws-sdk/client-kms` are included in the base runtime, no
   bundling needed):
   - Deploy the code in `lambda/generate-video/index.js`.
   - Add the environment variables above.
   - Grant the Lambda's execution role `dynamodb:PutItem` / `dynamodb:GetItem` on
     `CreatorSuiteRequests`, and `dynamodb:GetItem` / `dynamodb:UpdateItem` on
     `CreatorSuiteUserSettings`.
   - Grant `kms:Decrypt` on the KMS key created for `UserSettingsFunction` below (needed
     for the bring-your-own-key path — this Lambda decrypts a user's stored Higgsfield
     credentials to generate on their behalf).
5. **Add two API Gateway routes** (same HTTP API used for `/api/play`, `/api/upload-url`,
   `/api/submit`, same Cognito JWT authorizer):
   - `POST /api/generate-video` → `GenerateVideoFunction` (Lambda proxy integration)
   - `GET /api/generate-video/{requestId}` → `GenerateVideoFunction` (Lambda proxy integration)
6. **Test** with a valid Cognito ID token:
   ```
   curl -X POST https://<CF_DOMAIN>/api/generate-video \
     -H "Authorization: Bearer <cognito-id-token>" \
     -H "Content-Type: application/json" \
     -d '{"workflow":"text-to-video","prompt":"test","duration":5,"resolution":"480p","aspectRatio":"16:9"}'
   ```
   Repeat past `ENTERPRISE_MONTHLY_QUOTA_MINUTES` worth of `duration` seconds (converted to
   minutes) within the same calendar month (UTC) to confirm the 402 hard-stop fires:
   `{"error":"Monthly generation limit reached. Connect your own Higgsfield account in
   Settings to keep generating, or wait for your quota to renew."}`

---

# Required Setup for UserSettingsFunction (BYOK vs shared enterprise key)

Source: `lambda/user-settings/index.js`. Lets each user choose between the app's shared
Higgsfield key (metered, monthly quota enforced by `GenerateVideoFunction` above) and
their own Higgsfield account (BYOK — no quota, billed to their own Higgsfield account).
A user's own key is KMS-encrypted at rest and only ever decrypted server-side, inside
`GenerateVideoFunction`, at generation time; this Lambda never returns it once stored.

## Required Variables:
```
USER_SETTINGS_TABLE_NAME=CreatorSuiteUserSettings
HF_CREDENTIALS_KMS_KEY_ID=arn:aws:kms:<region>:<account-id>:key/<key-id>
ENTERPRISE_MONTHLY_QUOTA_MINUTES=30
```
`USER_SETTINGS_TABLE_NAME` and `ENTERPRISE_MONTHLY_QUOTA_MINUTES` must match the same
values configured on `GenerateVideoFunction` — both Lambdas read/write the same table
and must agree on the quota for the hard-stop to be consistent.

## What's stored

Two item shapes share the `CreatorSuiteUserSettings` table (PK `sub`, SK `settingType`):
- `{ sub, settingType: "profile", generationProvider: "enterprise"|"byok", hfCredentialsCiphertext? }`
- `{ sub, settingType: "usage#YYYY-MM", minutesUsed }` — one fresh item per calendar
  month, so the monthly quota reset needs no cron job: a new month is simply an item
  that doesn't exist yet.

## Setup Steps:

1. **Create a KMS key** (symmetric, "Encrypt and decrypt" usage) dedicated to this
   purpose — do not reuse a key that other, unrelated services can also call.
   - Grant `kms:Encrypt` to `UserSettingsFunction`'s execution role.
   - Grant `kms:Decrypt` to `GenerateVideoFunction`'s execution role (see above) — that
     Lambda is the only place a user's key is ever decrypted.
   - Neither Lambda needs the other permission — keep this split, don't grant both
     Encrypt and Decrypt to both roles.
2. **Create the `CreatorSuiteUserSettings` table** if not already done above:
   - Partition key: `sub` (String), sort key: `settingType` (String).
3. **Create the Lambda function** (Node.js 18.x+, same no-bundling note as above):
   - Deploy the code in `lambda/user-settings/index.js`.
   - Add the environment variables above.
   - Grant the Lambda's execution role `dynamodb:GetItem` / `dynamodb:PutItem` on
     `CreatorSuiteUserSettings`.
4. **Add three API Gateway routes** (same HTTP API, same Cognito JWT authorizer as
   `/api/generate-video`):
   - `GET /api/user-settings` → `UserSettingsFunction`
   - `POST /api/user-settings/higgsfield-key` → `UserSettingsFunction`
   - `DELETE /api/user-settings/higgsfield-key` → `UserSettingsFunction`
5. **Test** with a valid Cognito ID token:
   ```
   # Check current settings (defaults to enterprise, 0 used)
   curl https://<CF_DOMAIN>/api/user-settings \
     -H "Authorization: Bearer <cognito-id-token>"

   # Connect a personal Higgsfield key
   curl -X POST https://<CF_DOMAIN>/api/user-settings/higgsfield-key \
     -H "Authorization: Bearer <cognito-id-token>" \
     -H "Content-Type: application/json" \
     -d '{"keyId":"your-key-id","keySecret":"your-key-secret"}'
   # -> {"generationProvider":"byok","connected":true}

   # Disconnect, revert to the shared enterprise key
   curl -X DELETE https://<CF_DOMAIN>/api/user-settings/higgsfield-key \
     -H "Authorization: Bearer <cognito-id-token>"
   # -> {"generationProvider":"enterprise","connected":false}
   ```
   Confirm a generation submitted right after connecting a personal key actually uses
   it (check your own Higgsfield dashboard's usage for that key), and that quota keeps
   incrementing correctly for a user still on the enterprise path.

---

# Required Setup for Mobile-to-Web SSO Handoff (Creator Suite)

Source: `lambda/web-handoff/index.js`, `lambda/web-handoff-exchange/index.js`,
`lambda/cognito-triggers/*.js`. Lets the mobile app open the web Creator
Suite already signed in, without a second login prompt. Uses Cognito's
`CUSTOM_AUTH` (passwordless) flow — the mobile app mints a short-lived,
single-use code server-side; the web app exchanges it for a real session.

**This changes global Cognito User Pool behavior** (new Lambda triggers +
enabling a new auth flow on the app client) — read through before applying
in a pool that has other flows depending on default trigger behavior.

## 1. Create the DynamoDB table

Table name: `CreatorSuiteWebHandoffCodes` (separate from `CreatorSuiteRequests`).
- Partition key: `code` (String)
- Enable TTL on the `ttl` attribute (Number) — codes expire after 60s.

## 2. Deploy the two API Gateway Lambdas

**WebHandoffFunction** (`lambda/web-handoff/index.js`):
- Env vars: `HANDOFF_TABLE_NAME=CreatorSuiteWebHandoffCodes`, `WEB_APP_BASE_URL=https://<your-web-app-domain>`
- IAM: `dynamodb:PutItem` on `CreatorSuiteWebHandoffCodes`
- Route: `POST /api/web-handoff` — same Cognito JWT authorizer as `/api/generate-video`

**WebHandoffExchangeFunction** (`lambda/web-handoff-exchange/index.js`):
- Env vars: `HANDOFF_TABLE_NAME=CreatorSuiteWebHandoffCodes`, `COGNITO_CLIENT_ID=<cognitoClientId>`
- IAM: `dynamodb:GetItem` on `CreatorSuiteWebHandoffCodes`; `cognito-idp:InitiateAuth` and
  `cognito-idp:RespondToAuthChallenge` on the User Pool
- Route: `POST /api/web-handoff/exchange` — **no authorizer** (public route; security is the
  code's TTL + single-use + HTTPS)

## 3. Deploy the three Cognito Lambda triggers

- `lambda/cognito-triggers/defineAuthChallenge.js` → attach as the pool's **Define Auth Challenge** trigger
- `lambda/cognito-triggers/createAuthChallenge.js` → attach as the pool's **Create Auth Challenge** trigger
- `lambda/cognito-triggers/verifyAuthChallengeResponse.js` → attach as the pool's
  **Verify Auth Challenge Response** trigger
  - Env vars: `HANDOFF_TABLE_NAME=CreatorSuiteWebHandoffCodes`
  - IAM: `dynamodb:UpdateItem` on `CreatorSuiteWebHandoffCodes`

If the User Pool already has other Lambda triggers configured for these same
three slots, they need to be merged (one function per trigger slot) rather
than overwritten — check the Cognito console's "User pool properties" →
"Lambda triggers" section before attaching.

## 4. Enable CUSTOM_AUTH on the app client

In the User Pool's app client (the one `cognitoClientId` points to) →
"Authentication flows" → enable **`ALLOW_CUSTOM_AUTH`**.

## 5. Test end-to-end

```
# 1. Mint a code (needs a real signed-in user's ID token)
curl -X POST https://<CF_DOMAIN>/api/web-handoff \
  -H "Authorization: Bearer <cognito-id-token>"
# -> { "handoffUrl": "https://<web-app-domain>/auth/handoff?code=..." }

# 2. Exchange it (what the web app's /auth/handoff page does)
curl -X POST https://<CF_DOMAIN>/api/web-handoff/exchange \
  -H "Content-Type: application/json" \
  -d '{"code":"<code from step 1>"}'
# -> { "idToken": "...", "accessToken": "...", "refreshToken": "..." }
```

Confirm: repeating step 2 with the same code fails (single-use enforced via
the DynamoDB conditional update in `verifyAuthChallengeResponse.js`), and a
code older than 60s fails (TTL enforced).
