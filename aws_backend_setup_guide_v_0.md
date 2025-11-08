Amplify + AppSync + API Gateway + Lambda: Where Each Fits

You’ll still want a tiny REST edge in addition to Amplify + AppSync.

Think of it like this:

Amplify (Cognito) + AppSync (GraphQL) → user auth + your product data (users, submissions, reviews, tiers, etc.).

API Gateway + Lambda (behind CloudFront /api/*) → “edge‑y” tasks that must run on your server and can’t be done from the client or GraphQL alone:

mint CloudFront signed cookies for playback,

mint S3 pre‑signed upload URLs,

kick off MediaConvert jobs,

any future webhooks / EAS attestations / billing callbacks.

Where each piece fits
CloudFront

Origin A (S3, with OAC) → /assets/* (your HLS/MP4).
Restrict viewer access = Yes (trusted key group).

Origin B (API Gateway) → /api/*.
Restrict viewer access = No (so it can set cookies).
Cache policy = CachingDisabled; Origin request policy = AllViewerExceptHostHeader (for API Gateway).

API Gateway + Lambda (minimal routes)

GET /api/play/{assetId} → Lambda: Play
Reads CF private key from Secrets Manager, signs a policy for /assets/{assetId}/*, sets 3 cookies
(CloudFront-Policy, CloudFront-Signature, CloudFront-Key-Pair-Id) and returns manifestUrl.

GET /api/upload-url?assetId=...&filename=...&contentType=... → Lambda: UploadUrl
Returns a presigned PUT to s3://<bucket>/uploads/{assetId}/original.mp4.

POST /api/submit → Lambda: Submit
(optional) Validates user/tier, writes submission to AppSync/DynamoDB, and can start a MediaConvert job.

Secure these routes with an API Gateway JWT authorizer that trusts your Cognito User Pool (Amplify Auth).
Your mobile/web apps send the Authorization: Bearer <idToken> header on /api/*.

AppSync (GraphQL)

Use for all domain data:

models like Content, Submission, Review, Tier, UserEntitlement.

queries/mutations the app uses to show lists, statuses, reviews, etc.

Resolvers → DynamoDB (or Lambda if you need logic).
Amplify codegen will give you typed GraphQL calls in your apps.

Typical flows
Upload (client → review)

App (signed‑in via Amplify) calls GET https://<CF_DOMAIN>/api/upload-url?... with Cognito JWT.

Lambda returns presigned URL; client PUTs the file to S3 uploads/{assetId}/....

App calls an AppSync mutation to create/update the submission record.

(Optional) Lambda submit starts MediaConvert; status updates are stored in DynamoDB/AppSync.

Playback

App calls GET https://<CF_DOMAIN>/api/play/{assetId}?tier=free|standard|plus&mode=video|audio
→ Lambda sets CloudFront cookies and returns the manifestUrl.

Player loads the manifestUrl from https://<CF_DOMAIN>/assets/...; cookies authorize all segments.

Why not only AppSync?

GraphQL is great for data, but Set‑Cookie is an HTTP concern. AppSync isn’t ideal for issuing CloudFront cookies (and you don’t want your signing private key in a resolver reachable from the public internet). A tiny REST edge (API GW + Lambda) solves that cleanly and lives under the same CloudFront domain.

AWS Backend Setup Guide (v0.7)

This is your end‑to‑end checklist to stand up the streaming backend + provenance pipeline on AWS, wire it to the on‑chain scripts we built, and test playback (video + audio‑only).


# AWS Backend Setup Guide (v0.7)

This is your end‑to‑end checklist to stand up the **streaming backend + provenance pipeline** on AWS, wire it to the on‑chain scripts we built, and test playback (video + audio‑only).

---

## Backend Architecture Overview

### Complete System Architecture

The mAIScreen backend uses a **hybrid architecture** combining **AppSync (GraphQL)** for data operations and **API Gateway (REST)** for edge tasks that require HTTP-specific features.

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Mobile/Web Client                            │
│                  (iOS, Android, Web Applications)                     │
└────────────────────┬──────────────────┬─────────────────────────────┘
                     │                  │
                     │ HTTPS            │ HTTPS
                     ▼                  ▼
         ┌─────────────────────┐   ┌─────────────────────┐
         │   CloudFront (CDN)   │   │  CloudFront (CDN)    │
         │   cdn.example.com    │   │   cdn.example.com    │
         └───────────┬───────────┘   └───────────┬───────────┘
                     │                          │
        ┌────────────┴────────────┐  ┌──────────┴─────────────┐
        │                         │  │                        │
        ▼                         ▼  ▼                        ▼
┌───────────────┐         ┌───────────────┐          ┌───────────────┐
│  Origin A     │         │  Origin B     │          │  Origin C     │
│  S3 Bucket    │         │  API Gateway │          │  AppSync      │
│  /assets/*   │         │  /api/*      │          │  (GraphQL)     │
│  (Private)    │         │  (REST)      │          │  /graphql     │
└───────┬───────┘         └───────┬──────┘          └───────┬───────┘
        │                          │                        │
        │                          │                        │
        │                          ▼                        ▼
        │                  ┌───────────────┐        ┌───────────────┐
        │                  │   Lambda      │        │  DynamoDB     │
        │                  │   Functions   │        │  Tables       │
        │                  │               │        │               │
        │                  │ • Play        │        │ • Users       │
        │                  │ • UploadUrl   │        │ • Content    │
        │                  │ • Submit      │        │ • Submissions│
        │                  └───────┬───────┘        │ • Reviews     │
        │                          │                └───────────────┘
        │                          │
        │                          ▼
        │                  ┌───────────────┐
        │                  │ Secrets Mgr   │
        │                  │ • CF Keys     │
        │                  │ • API Keys    │
        └──────────────────┴───────────────┘
```

### Request Flow Architecture

#### 1. **Authentication & Authorization Layer**
```
Client App
  │
  │ 1. Login Request
  ▼
AWS Cognito (via Amplify)
  │
  │ 2. JWT Token (idToken, accessToken)
  ▼
Client App (stores tokens)
  │
  │ 3. Subsequent requests include: Authorization: Bearer <idToken>
  ▼
API Gateway / AppSync (validates JWT)
```

#### 2. **Data Operations Flow (AppSync GraphQL)**
```
Mobile/Web App
  │
  │ GraphQL Query/Mutation
  │ Authorization: Bearer <idToken>
  ▼
CloudFront (/graphql endpoint)
  │
  │ Routes to AppSync
  ▼
AWS AppSync
  │
  │ Executes Resolvers
  ├─► Direct DynamoDB Resolver ──► DynamoDB Tables
  └─► Lambda Resolver ──────────► Lambda Function ──► DynamoDB/Other Services
  │
  │ Returns GraphQL Response
  ▼
Client App
```

**Use Cases for AppSync:**
- User profile queries
- Content listing and search
- Submission CRUD operations
- Reviews and ratings
- Watchlist management
- Subscription management
- All data that fits the GraphQL query/mutation pattern

#### 3. **Edge Operations Flow (API Gateway REST)**
```
Mobile/Web App
  │
  │ HTTP REST Request
  │ GET /api/play/{assetId}?tier=free&mode=video
  │ Authorization: Bearer <idToken>
  ▼
CloudFront (/api/* pattern)
  │
  │ Routes to API Gateway
  ▼
API Gateway (HTTP API v2)
  │
  │ 1. JWT Authorizer validates token
  │    ├─ Issuer: Cognito User Pool
  │    └─ Audience: App Client ID
  │
  │ 2. If authorized, routes to Lambda
  ▼
Lambda Function (Play)
  │
  │ 1. Reads user's subscription tier from AppSync/DynamoDB
  │ 2. Retrieves CloudFront private key from Secrets Manager
  │ 3. Signs CloudFront cookies for /assets/{assetId}/*
  │ 4. Generates tier-appropriate manifest URL
  │
  │ Returns JSON:
  │ {
  │   "ok": true,
  │   "manifestUrl": "https://cdn.example.com/assets/ASSET001/hls/master_free.m3u8",
  │   "assetId": "ASSET001",
  │   "tier": "free"
  │ }
  │
  │ Sets HTTP Headers:
  │ Set-Cookie: CloudFront-Policy=...
  │ Set-Cookie: CloudFront-Signature=...
  │ Set-Cookie: CloudFront-Key-Pair-Id=...
  ▼
API Gateway
  │
  │ Converts Lambda response to HTTP response
  │ Preserves Set-Cookie headers
  ▼
CloudFront
  │
  │ Returns response with cookies to client
  ▼
Client App
  │
  │ Uses manifestUrl to request HLS segments
  │ Includes cookies in requests to /assets/*
  ▼
CloudFront
  │
  │ Validates signed cookies
  │ If valid, serves from S3 (via OAC)
  ▼
Client App (plays video)
```

### API Gateway Route Integrations

#### Route Configuration Pattern

Each API Gateway route connects to a Lambda function via **Lambda Proxy Integration**:

```
API Gateway Route
  │
  ├─ Route Definition
  │   GET /api/play/{assetId}
  │   Query Parameters: tier, mode
  │
  ├─ Authorization
  │   Type: JWT Authorizer
  │   Issuer: https://cognito-idp.{region}.amazonaws.com/{userPoolId}
  │   Audience: {appClientId}
  │   Authorization: Required
  │
  ├─ Integration
  │   Type: Lambda Proxy Integration
  │   Lambda Function: PlayFunction
  │   Invoke with full request context
  │
  └─ Response
      Status Code: 200
      Headers: Set-Cookie (multiple)
      Body: JSON
```

#### Complete Route List

| Route | Method | Lambda Function | Purpose |
|-------|--------|----------------|---------|
| `/api/play/{assetId}` | GET | `PlayFunction` | Generate CloudFront signed cookies and return manifest URL |
| `/api/upload-url` | GET | `UploadUrlFunction` | Generate S3 pre-signed PUT URL for direct upload |
| `/api/submit` | POST | `SubmitFunction` | Process submission, validate tier, trigger MediaConvert |

### Why Two APIs? (AppSync vs API Gateway)

#### **AppSync (GraphQL)** - Use For:
✅ **All CRUD data operations**
- User data management
- Content catalog queries
- Submission lifecycle
- Reviews and ratings
- Watchlists and favorites
- Subscription management

✅ **Benefits:**
- Strongly typed queries
- Auto-generated client code (Amplify)
- Real-time subscriptions
- Built-in authorization with Cognito
- Automatic caching
- Single endpoint for all data operations

#### **API Gateway (REST)** - Use For:
✅ **HTTP-specific operations that can't be done in GraphQL:**
- Setting HTTP cookies (CloudFront signed cookies)
- Generating S3 pre-signed URLs
- Media processing triggers (MediaConvert)
- Webhook callbacks
- Binary file uploads (though direct S3 is preferred)

✅ **Benefits:**
- Fine-grained control over HTTP headers
- Cookie management
- Custom response formatting
- Legacy/third-party integration support

### Integration Points

#### 1. **Authentication Integration**
Both APIs share the same authentication:
```
Cognito User Pool
  │
  ├─► AppSync Authorization
  │   Uses @aws_auth directive
  │   Validates JWT automatically
  │
  └─► API Gateway Authorization
      Uses JWT Authorizer
      Validates same JWT token
      Extracts user info from token claims
```

#### 2. **Data Integration**
Lambda functions can call AppSync:
```
Lambda Function
  │
  │ GraphQL Mutation/Query
  │ (using AWS AppSync SDK)
  ▼
AppSync
  │
  │ Executes resolvers
  ▼
DynamoDB
```

#### 3. **Secrets Integration**
Both can access Secrets Manager:
```
Lambda Functions
  │
  │ AWS SDK Secrets Manager
  ▼
Secrets Manager
  │
  ├─ CloudFront Private Keys
  ├─ API Keys (third-party)
  └─ Database Credentials
```

### Complete Request Example: Video Playback

```
1. User opens video player in app
   └─► App calls: GET /api/play/ASSET001?tier=free&mode=video
       Headers: Authorization: Bearer <idToken>

2. CloudFront receives request
   └─► Routes to: API Gateway /api/* origin

3. API Gateway processes request
   ├─► Validates JWT token with Cognito
   ├─► Extracts user ID from token
   └─► Invokes: PlayFunction Lambda

4. Lambda Function execution
   ├─► Reads user subscription from DynamoDB (or calls AppSync)
   ├─► Retrieves CloudFront private key from Secrets Manager
   ├─► Signs cookies for /assets/ASSET001/*
   ├─► Generates manifest URL: /assets/ASSET001/hls/master_free.m3u8
   └─► Returns JSON response + Set-Cookie headers

5. API Gateway responds
   └─► Returns HTTP 200 with cookies and manifest URL

6. Client app receives response
   ├─► Stores cookies automatically (HttpOnly, Secure)
   └─► Requests manifest: GET /assets/ASSET001/hls/master_free.m3u8
       Headers: Cookie: CloudFront-Policy=...; CloudFront-Signature=...; ...

7. CloudFront validates cookies
   ├─► Checks cookie signature
   ├─► Validates policy (expiry, path restrictions)
   └─► If valid: Serves from S3 via OAC

8. Client receives manifest and segments
   └─► Video player plays content
```

### CloudFront Behavior Configuration

CloudFront acts as the **single entry point** routing requests based on path patterns:

| Path Pattern | Origin | Cache Behavior | Purpose |
|--------------|--------|----------------|---------|
| `/assets/*` | S3 Bucket (OAC) | Cache enabled | Serve media files (HLS, DASH, MP4) |
| `/api/*` | API Gateway | No cache | REST API endpoints |
| `/graphql` | AppSync | No cache | GraphQL endpoint |

**Key Configuration:**
- **Origin A (S3)**: Requires signed cookies (CloudFront signed URLs/cookies)
- **Origin B (API Gateway)**: No viewer restrictions (needs to set cookies)
- **Origin C (AppSync)**: Uses AppSync's built-in authentication

### Security & Authorization Flow

```
Client Request
  │
  ├─► Contains JWT Token: Authorization: Bearer <idToken>
  │
  ▼
API Gateway / AppSync
  │
  ├─► Validates JWT against Cognito User Pool
  │   ├─ Signature verification
  │   ├─ Expiry check
  │   └─ Audience validation
  │
  ├─► Extracts User Claims:
  │   ├─ sub (user ID)
  │   ├─ email
  │   └─ cognito:groups (for role-based access)
  │
  └─► If valid:
      ├─► API Gateway: Invokes Lambda with user context
      └─► AppSync: Executes resolver with user identity
```

### Lambda Function Integration Pattern

Each Lambda function receives the full request context from API Gateway:

```typescript
// Lambda Function Structure
export const handler = async (event: APIGatewayProxyEvent) => {
  // 1. Extract user from JWT (already validated by API Gateway)
  const userId = event.requestContext.authorizer.claims.sub;
  const email = event.requestContext.authorizer.claims.email;
  
  // 2. Extract route parameters
  const assetId = event.pathParameters?.assetId;
  const tier = event.queryStringParameters?.tier;
  const mode = event.queryStringParameters?.mode;
  
  // 3. Business logic
  const result = await processRequest(userId, assetId, tier, mode);
  
  // 4. Return formatted response
  return {
    statusCode: 200,
    headers: {
      'Content-Type': 'application/json',
      'Set-Cookie': result.cookies // API Gateway handles multiple Set-Cookie
    },
    body: JSON.stringify(result.data)
  };
};
```

### AppSync Resolver Integration Pattern

AppSync resolvers can connect directly to DynamoDB or use Lambda for complex logic:

```graphql
# Direct DynamoDB Resolver
type Query {
  getContent(contentId: ID!): Content
}

# Resolver Mapping Template (VTL)
{
  "version": "2017-02-28",
  "operation": "GetItem",
  "key": {
    "contentId": $util.dynamodb.toDynamoDBJson($ctx.args.contentId)
  }
}
```

```graphql
# Lambda Resolver (for complex logic)
type Mutation {
  createSubmission(input: CreateSubmissionInput!): Submission
}

# Resolver invokes Lambda with mutation arguments
```

### Data Flow Summary

**GraphQL Operations (AppSync):**
```
Client → CloudFront → AppSync → DynamoDB → Response
         (automatic)   (validates JWT)  (resolvers)
```

**REST Operations (API Gateway):**
```
Client → CloudFront → API Gateway → Lambda → DynamoDB/Secrets Manager → Response
         (routes)      (validates JWT)  (business logic)  (data/keys)
```

**Media Streaming:**
```
Client → CloudFront → S3 (via OAC)
         (validates cookies)  (serves media)
```

### When to Use Which API

**Use AppSync when:**
- ✅ Querying data (lists, single items, searches)
- ✅ Creating/updating records (users, content, submissions)
- ✅ Need real-time updates (subscriptions)
- ✅ Working with relationships (user → submissions → reviews)
- ✅ Need automatic code generation

**Use API Gateway when:**
- ✅ Need to set HTTP cookies
- ✅ Generating pre-signed URLs
- ✅ Triggering async jobs (MediaConvert, Step Functions)
- ✅ Webhook callbacks
- ✅ Operations that don't fit GraphQL patterns

### Summary

The architecture uses **AppSync for data** and **API Gateway for edge operations**, both secured by the same Cognito authentication. CloudFront provides a single domain for all requests, routing to the appropriate backend service based on the path pattern. This hybrid approach gives you the benefits of GraphQL for data operations while maintaining flexibility for HTTP-specific requirements.

---

## 0) Prerequisites
- **AWS account** with admin access (for initial setup only).
- **Node.js 20+**, **npm** (or pnpm), **TypeScript** + **ts-node**.
- A **testnet wallet** + RPC URL (e.g., Base Sepolia) for on‑chain scripts.
- Optional: a domain (e.g., `example.com`) and a subdomain for CDN (`cdn.example.com`).

**Local installs**
```bash
npm i -g typescript ts-node
```

---

## 1) S3 Buckets & Folder Layout
Create a single private VOD bucket (example: `your-vod-bucket`) with these prefixes:
```
s3://your-vod-bucket/uploads/                 # raw uploads
s3://your-vod-bucket/assets/{assetId}/hls/    # video HLS renditions
s3://your-vod-bucket/assets/{assetId}/dash/   # video DASH renditions
s3://your-vod-bucket/assets/{assetId}/hls_audio/   # audio-only HLS
s3://your-vod-bucket/assets/{assetId}/dash_audio/  # audio-only DASH
s3://your-vod-bucket/assets/{assetId}/thumbs/
```
**Enable bucket:** Block Public Access **ON**.

**CORS** (allow range requests for HLS/DASH)
```json
[
  {
    "AllowedHeaders": ["*", "Range"],
    "AllowedMethods": ["GET", "HEAD"],
    "AllowedOrigins": ["https://cdn.example.com", "https://app.example.com"],
    "ExposeHeaders": ["ETag", "Accept-Ranges", "Content-Range"],
    "MaxAgeSeconds": 3000
  }
]
```

**Lifecycle** (suggested): move `uploads/` → **S3 Standard‑IA** at 30–60 days; archive later to Glacier if desired.

---

## 2) CloudFront (CDN) with Origin Access Control (OAC)
1. Create a **CloudFront distribution** with the S3 bucket as origin.
2. Enable **Origin Access Control**, attach to origin; keep the bucket private.
3. Create a **Key Group** and a **Public Key** (RSA). Note the **Key Pair ID** and store the **private key** in **Secrets Manager**.
4. Cache behavior: allow `GET/HEAD`, require **signed cookies** for private paths (we’ll issue them from the API).
5. Set the alternate domain name to `cdn.example.com` and attach an ACM cert.

**Paths we’ll serve (examples)**
```
/assets/*/hls/*
/assets/*/dash/*
/assets/*/hls_audio/*
/assets/*/dash_audio/*
```

---

## 3) IAM & Secrets
- **MediaConvert role**: allow read from `uploads/` and write to `assets/`. Attach AWS managed `MediaConvertFullAccess` and an S3 policy scoped to your bucket prefixes.
- **API execution role** (if Lambda) or task role (if ECS): permissions to read **Secrets Manager** (CloudFront private key), and (optional) S3 read for any app features.
- **KMS/Secrets Manager**: store `CF_PRIVATE_KEY`, RPC keys, and IPFS tokens.

---

## 4) MediaConvert: Queues & Job Templates
Create one **Queue** and these **job templates** using the JSON we provided earlier:
- **Video HLS (CMAF) ladder**: 240p→1080p (optional 2160p)
- **Video DASH (CMAF) ladder**
- **Audio‑only HLS** (64k/96k/128k/192k)
- **Audio‑only DASH**

Destination paths must be under `s3://your-vod-bucket/assets/{assetId}/...`.

(Optional) Add **EventBridge** rules to trigger a Lambda/Step Functions state machine after job completion (to write master playlists, thumbnails, DB rows, etc.).

---

## 5) Minimal API (Express or Lambda)
**Goal**: return a **tiered master manifest URL** and set **CloudFront signed cookies** scoping which rendition folders the user can fetch.

- If using **Express** (ECS/Fargate or EC2): use the `server/playback.js` example provided earlier.
- If using **Lambda + API Gateway**: same logic; for cookies, return `Set-Cookie` headers via API Gateway (enable multi‑value headers).

**Env vars**
```
CF_DOMAIN=cdn.example.com
CF_KEY_PAIR_ID=KXXXXXXXXXXXX
CF_PRIVATE_KEY=<PEM string or load from Secrets Manager>
AWS_REGION=us-east-1
```

**DynamoDB** (suggested table `Assets`):
```json
{
  "assetId": "ASSET123",
  "title": "Demo Neon City",
  "tierMax": "plus",
  "acceptsAudioOnly": true,
  "allowedAudioBitrates": { "free": ["64k","96k","128k"], "standard": ["64k","96k","128k","192k"], "plus": ["64k","96k","128k","192k"] }
}
```

---

## 6) Contracts & EAS
- **Deploy** `ProvenanceRegistry.sol` and `FilmAuthorship721.sol` in **Remix** (as per the Remix guide in the other canvas) to your target network.
- **Register EAS Schemas** (review + rating) and capture schema UIDs.
- Keep these in your `.env`: `REGISTRY`, `EAS_CONTRACT`, `SCHEMA_UID`, `RATING_SCHEMA_UID`, `RPC_URL`, `PRIVATE_KEY`.

---

## 7) Scripts & Libraries (install)
From your repo root (the one with `scripts/`):
```bash
npm i ethers @ethereum-attestation-service/eas-sdk merkletreejs keccak256 yargs axios \
  web3.storage form-data @aws-sdk/client-s3 aws-cloudfront-sign express cookie
```
**Dev**: `ts-node`, `typescript`.

**What each is for**
- `ethers`, `eas-sdk` — on‑chain calls + attestations
- `merkletreejs`, `keccak256` — compute `contentRootHash`
- `web3.storage` or pinning client of your choice — IPFS pin (optional in local)
- `@aws-sdk/client-s3` — write master playlists/aux files to S3
- `aws-cloudfront-sign`, `cookie` — signed cookies for CDN gating
- `express` — minimal API server (if not using Lambda)

---

## 8) Environment (.env)
```
# Chain
RPC_URL=https://...           # Base Sepolia (test) or Base/Polygon (prod)
PRIVATE_KEY=0x...
REGISTRY=0x...
EAS_CONTRACT=0x...            # network-specific
SCHEMA_UID=0x...
RATING_SCHEMA_UID=0x...

# IPFS (optional for live)
PINATA_JWT=eyJ...             # or WEB3_STORAGE_TOKEN=...

# AWS/CDN
AWS_REGION=us-east-1
S3_BUCKET=your-vod-bucket
CF_DOMAIN=cdn.example.com
CF_KEY_PAIR_ID=KXXXXXXXXXXXX
CF_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n"
```

---

## 9) Ingestion & Registration (three paths)
**A) Local‑only (no IPFS, placeholder CID)**
```bash
npx ts-node scripts/ingest.ts \
  --assetDir ./uploads \
  --filmUID flm_demo --version v1 \
  --creator 0xYourWallet --registry $REGISTRY --rpc $RPC_URL --privateKey $PRIVATE_KEY \
  --platform chatgpt --app web --provider openai --model gpt-4.1 \
  --provenanceCID "local://provenance/provenance_demo.json" \
  --yes
```

**B) Live (script pins + attests)**
```bash
export PINATA_JWT=...
npx ts-node scripts/ingest.ts \
  --assetDir ./uploads \
  --filmUID flm_live --version v1 \
  --creator 0xYourWallet --registry $REGISTRY --rpc $RPC_URL --privateKey $PRIVATE_KEY \
  --platform chatgpt --app web --provider openai --model gpt-4.1 \
  --attest --schemaUid $SCHEMA_UID --rateAttest --ratingSchemaUid $RATING_SCHEMA_UID \
  --yes
```

**C) Flip later (local → IPFS)**
```bash
# After pinning provenance JSON
npx ts-node scripts/amend.ts \
  --registry $REGISTRY --rpc $RPC_URL --privateKey $PRIVATE_KEY \
  --filmIdHash 0xFILM... \
  --newCID ipfs://bafy... \
  --newRoot 0x<contentRootHash>
```

**Resuming steps**: `--resume-from attest` (or any step) and `--dry-run` to preview payloads.

---

## 10) Transcoding & Masters
1. Upload raw media to `s3://your-vod-bucket/uploads/{assetId}/original.mp4`.
2. Run **MediaConvert** job with your video HLS/DASH templates (destination under `assets/{assetId}/...`).
3. Generate master playlists:
```bash
node tools/makeMasters.mjs your-vod-bucket ASSET123
node tools/makeAudioMaster.mjs your-vod-bucket ASSET123   # if audio-only
```
4. (Optional) Thumbnails/sprites, captions, etc.

---

## 11) Playback Test (video + audio‑only)
- Start your API (Express) or deploy Lambda route.
- **Video**: `GET /play/ASSET123` → returns tiered `manifestUrl` + sets signed cookies.
- **Audio‑only**: `GET /play/ASSET123?mode=audio` → returns `hls_audio/master_audio.m3u8`.
- In the web app, drop in **AudioModeTogglePlayer.tsx** (from the other canvas) and set `assetId`.

---

## 12) Security & Ops
- Store secrets in **Secrets Manager**; never commit private keys.
- Enable **CloudFront logs** + **WAF** (basic rate limiting and bot blocks).
- Add **S3 lifecycle** and **bucket policies** to restrict non‑CDN access.
- Tag resources (`project=ai-film`, `env=dev/prod`).

---

## 13) Optional Enhancements
- **Step Functions** pipeline: upload → validate → hash/JSON → (pin) → register → MediaConvert → write masters → DB → notify.
- **DRM** later via MediaPackage VOD + SPEKE. Start with signed cookies.
- **Cost guardrails**: budget alerts, per‑tier bandwidth caps (by setting shorter cookie expiries on Free, etc.).

---

## 14) Quick Library Index
- **Backend runtime**: Node.js 20+
- **API**: `express`, `aws-cloudfront-sign`, `cookie`, `@aws-sdk/client-s3`
- **On‑chain**: `ethers`, `@ethereum-attestation-service/eas-sdk`
- **Data structures**: `merkletreejs`, `keccak256`
- **Pinning** (optional): `web3.storage` or provider SDK
- **CLI**: `yargs`, `axios`
- **Frontend**: `hls.js`, React + shadcn/ui (for the toggle player)

---
AI Film Platform — Data & API (MVP)

This document defines the core data models, auth rules, and the edge API used to upload, review, and stream AI-generated content.

MVP: what each model is for

UserProfile – one row per user; display handle + prefs. id = Cognito sub.

Entitlement – user’s tier (Free/Standard/Plus) and monthly minute quota.

Program – a “title page”: movie, series, short, reel, or audio series.

Episode – individual episodes (only for series).

MediaAsset – S3 keys for master playlists/MP4/thumbs; transcode status; provenance hooks.

Submission – creator uploads + metadata; drives review workflow.

Review – moderation results (decision, rating, notes).

Tag – genres/topics attached to programs.

Enums
enum Tier { FREE STANDARD PLUS }
enum ContentKind { MOVIE SERIES EPISODE SHORT REEL AUDIO_SERIES }
enum SubmissionStatus { DRAFT SUBMITTED IN_REVIEW CHANGES_REQUESTED APPROVED REJECTED PUBLISHED }
enum ReviewDecision { APPROVE REJECT CHANGES_REQUESTED }
enum MaturityRating { G PG PG13 R NC17 TVY TVG TVPG TV14 TVMA UNRATED }
enum PromptPlatform { VEO3 RUNWAY PIKA MIDJOURNEY CUSTOM OTHER }

GraphQL schema (paste into schema.graphql)

Notes:

@model automatically adds createdAt and updatedAt fields. Don’t redeclare them.

id on UserProfile is the Cognito sub (see “UserProfile creation” below).

Public read via provider: apiKey is optional; remove if you want “signed-in only” apps.

type UserProfile
@model
@auth(rules: [
{ allow: owner, ownerField: "id", identityClaim: "sub" },   # owner is the user whose sub == id
{ allow: groups, groups: ["admin"], operations: [read, update, delete] }
]) {
id: ID!                 # must be set to Cognito sub
handle: String
avatarUrl: AWSURL
bio: String
entitlement: Entitlement @hasOne
}

type Entitlement
@model
@auth(rules: [
{ allow: owner },
{ allow: groups, groups: ["admin","moderator"], operations: [read, update] }
]) {
id: ID!
userId: ID! @index(name: "byUser", queryField: "entitlementByUser")
tier: Tier!
minutesQuotaMonthly: Int!      # e.g., 30 / 75 / 150
minutesUsedThisMonth: Int!
renewsAt: AWSDateTime!
}

type Program
@model
@auth(rules: [
{ allow: groups, groups: ["admin","moderator"] },                   # staff: full access
{ allow: public, operations: [read], provider: apiKey },            # optional public read
{ allow: owner, operations: [create, read, update] }                # owner edits until published
]) {
id: ID!
kind: ContentKind!
title: String!
logline: String
synopsis: String
tags: [Tag] @manyToMany(relationName: "ProgramTags")
posterKey: String
heroKey: String
maturityRating: MaturityRating
isPublished: Boolean! @index(name: "byPublished")
publishedAt: AWSDateTime
owner: ID
primaryAsset: MediaAsset @hasOne
episodes: [Episode] @hasMany(indexName: "byProgram", fields: ["id"])
}

type Episode
@model
@auth(rules: [
{ allow: groups, groups: ["admin","moderator"] },
{ allow: owner, operations: [create, read, update] },
{ allow: public, operations: [read], provider: apiKey }             # optional public read
]) {
id: ID!
programId: ID! @index(name: "byProgram", sortKeyFields:["seasonNumber","episodeNumber"])
program: Program @belongsTo(fields: ["programId"])
seasonNumber: Int!
episodeNumber: Int!
title: String!
synopsis: String
runtimeSec: Int
asset: MediaAsset @hasOne
publishedAt: AWSDateTime
}

type MediaAsset
@model
@auth(rules: [
{ allow: groups, groups: ["admin","moderator"] },
{ allow: owner, operations: [create, read, update] },
{ allow: public, operations: [read], provider: apiKey }             # safe if /assets gated by CloudFront
]) {
id: ID!
assetId: String! @index(name: "byAssetId")                             # matches s3://.../assets/{assetId}/
durationSec: Int
width: Int
height: Int
hlsMasterKey: String               # e.g., assets/ASSET001/hls/master_free.m3u8
audioHlsMasterKey: String          # e.g., assets/ASSET001/hls_audio/master_audio.m3u8
mp4Key: String
thumbsPrefix: String
transcodeStatus: String            # QUEUED | PROCESSING | SUCCEEDED | ERROR
mediaconvertJobId: String
filmIdHash: String
contentRootHash: String
provenanceCID: String              # ipfs://...
}

type Submission
@model
@auth(rules: [
{ allow: owner, operations: [create, read, update] },
{ allow: groups, groups: ["admin","moderator"] }
])
@index(name: "byOwner", queryField: "submissionsByOwner", sortKeyFields:["createdAt"])
@index(name: "byStatus", queryField: "submissionsByStatus") {
id: ID!
owner: ID!                     # Cognito sub
status: SubmissionStatus!
programDraftId: ID             # link to Program created on approval (optional)
title: String!
kind: ContentKind!             # MOVIE/SERIES/EPISODE/SHORT/REEL/AUDIO_SERIES
runtimeSec: Int
tierAtSubmission: Tier!
promptPlatform: PromptPlatform!
promptOrigin: String           # e.g., "Veo 3"
promptTextHash: String         # sha256 of prompt (not raw text)
toolsUsed: [String]
sourceNotes: String
uploadKey: String              # key only (uploads/ASSET001/original.mp4)
assetId: String                # target assets/{assetId}/
reviews: [Review] @hasMany(indexName: "bySubmission", fields: ["id"])
}

type Review
@model
@auth(rules: [
{ allow: groups, groups: ["admin","moderator"] },
{ allow: owner, operations: [read] }                                 # creator can read decisions
])
@index(name: "bySubmission", queryField: "reviewsBySubmission") {
id: ID!
submissionId: ID!
submission: Submission @belongsTo(fields: ["submissionId"])
reviewerId: ID!
decision: ReviewDecision!
maturityRating: MaturityRating
notes: String
}

type Tag
@model
@auth(rules: [
{ allow: public, operations: [read], provider: apiKey },
{ allow: groups, groups: ["admin","moderator"] }
]) {
id: ID!
name: String! @index(name: "byName")
programs: [Program] @manyToMany(relationName: "ProgramTags")
}

Nice-to-have extensions
type ViewingProgress
@model
@auth(rules:[{allow: owner},{allow: groups, groups:["admin","moderator"], operations:[read]}])
@index(name:"byUserAndProgram", queryField:"progressByUserProgram", sortKeyFields:["programId"]) {
id: ID!
userId: ID!
programId: ID!
episodeId: ID
positionSec: Int!
updatedAt: AWSDateTime
}

type UserRating
@model
@auth(rules:[{allow: owner},{allow: groups, groups:["admin","moderator"], operations:[read]}])
@index(name:"byProgram", queryField:"ratingsByProgram") {
id: ID!
programId: ID!
userId: ID!
score: Int!        # 1..5
createdAt: AWSDateTime
}

type Watchlist
@model
@auth(rules:[{allow: owner}])
@index(name:"byUser", queryField:"watchlistByUser") {
id: ID!
userId: ID!
programId: ID!
createdAt: AWSDateTime
}

type Collection
@model
@auth(rules:[
{allow: groups, groups:["admin","moderator"]},
{allow: public, operations:[read], provider: apiKey}
]) {
id: ID!
title: String!
slug: String! @index(name:"bySlug")
items: [Program] @manyToMany(relationName: "CollectionPrograms")
published: Boolean!
order: Int
}

Auth & groups

Cognito groups: admin, moderator, creator.

Public read (optional): keep on Program/Episode/Tag/Collection or remove for a closed app.

Owner rules: creators see/edit their own Program (pre-publish) and their Submission; staff can moderate.

UserProfile creation pattern

AppSync doesn’t auto-create a profile when a user signs up. Choose one:

A) Create on first sign-in (simple)

After login, get sub.

getUserProfile(id: sub); if none, createUserProfile({ id: sub, handle }).

B) Post-Confirmation trigger (hands-off)

Add a Cognito Post Confirmation Lambda that calls createUserProfile with id = event.request.userAttributes.sub.

Keep id = sub. It makes owner auth trivial and joins clean.

Streaming: how the models tie to the edge

MediaAsset.assetId == folder under s3://<bucket>/assets/{assetId}/....

Play Lambda checks Entitlement.tier, signs CloudFront cookies scoped to /assets/{assetId}/*, and returns the manifestUrl (free/standard/plus or audio-only).

Upload-URL Lambda returns a pre-signed PUT URL for uploads/{assetId}/original.mp4 (or similar), so clients upload directly to S3 without exposing the bucket.

Edge/API wiring checklist
API Gateway (HTTP API v2)

Routes

GET /api/play/{assetId} → Lambda play

GET /api/upload-url → Lambda upload-url

POST /api/submit → Lambda submit (optional)

JWT Authorizer (Cognito)

Issuer: https://cognito-idp.<region>.amazonaws.com/<userPoolId>

Audience: your App Client ID(s)

Attach to the routes; set Authorization = Required.

Stage: $default with Auto-deploy ON.

Lambda Set-Cookie (HTTP API v2)

Return a cookies: [] array; API GW will emit multiple Set-Cookie headers.

CloudFront

Origin A (S3 + OAC) → /assets/*

Restrict viewer access: Yes

Trusted key groups: your key group (public key)

Origin B (API Gateway HTTP API) → /api/*

Restrict viewer access: No

Cache policy: Managed – CachingDisabled

Origin request policy: Managed – AllViewerExceptHostHeader

Allowed methods: GET/HEAD (+ POST/OPTIONS if needed)

Lambdas (contracts)

play → returns cookies + manifest

{
"ok": true,
"assetId": "ASSET001",
"tier": "free|standard|plus",
"mode": "video|audio",
"expiresAt": "2025-01-01T00:00:00Z",
"manifestUrl": "https://<CF_DOMAIN>/assets/ASSET001/hls/master_free.m3u8"
}


Sets three cookies: CloudFront-Policy, CloudFront-Signature, CloudFront-Key-Pair-Id (Secure; HttpOnly; SameSite=Lax).

upload-url → returns presigned PUT

{
"method": "PUT",
"uploadUrl": "https://<bucket>.s3.amazonaws.com/uploads/ASSET001/....?X-Amz-Algorithm=...",
"s3Key": "uploads/ASSET001/1707090000_original.mp4",
"expiresInSec": 900,
"requiredHeaders": { "Content-Type": "video/mp4" }
}

Minimal environment variables

play: CF_DOMAIN, CF_KEY_PAIR_ID, CF_PRIVATE_KEY (or CF_PRIVATE_KEY_SECRET_ARN), COOKIE_TTL_SEC

upload-url: S3_BUCKET, UPLOAD_URL_TTL

IAM (least privilege)

play: if using Secrets Manager
secretsmanager:GetSecretValue on your private key secret.

upload-url:
s3:PutObject on arn:aws:s3:::<bucket>/uploads/*
(add multipart permissions later if needed).

Test quickly
# 1) Call play THROUGH CloudFront; you should see 3 Set-Cookie headers:
curl -s -D - "https://<CF_DOMAIN>/api/play/ASSET001?tier=free" -o /dev/null | grep -i '^Set-Cookie:'

# 2) Then the manifest should be 200 with those cookies:
curl -I "https://<CF_DOMAIN>/assets/ASSET001/hls/master_free.m3u8" \
-H 'Cookie: CloudFront-Policy=...; CloudFront-Signature=...; CloudFront-Key-Pair-Id=KXXXX'

Security notes

Keep cookie TTLs short (e.g., 1h) and scope the policy to /assets/{assetId}/*.

Store the CloudFront private key in Secrets Manager for prod; env var is OK for dev.

Add WAF to the CloudFront distribution; enable logging.

For web, CORS should allow only your app’s origins and credentials.


### Success Criteria (sanity checklist)
- ✅ CloudFront serves `/assets/ASSET123/hls/master_free.m3u8` but **only** with signed cookies.
- ✅ `/play/:assetId` returns a manifest URL and sets cookies with a 1‑hour expiry.
- ✅ MediaConvert outputs in the correct folders; masters are written by the tools.
- ✅ Registry entry on-chain matches `provenanceCID`, `contentRootHash`, and `promptOriginHash`.
- ✅ EAS review + rating attestations visible for your `filmIdHash`.

That’s it. You can now upload → transcode → register → attest → stream, all on AWS with provenance anchored on-chain.

