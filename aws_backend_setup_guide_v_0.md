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

### Success Criteria (sanity checklist)
- ✅ CloudFront serves `/assets/ASSET123/hls/master_free.m3u8` but **only** with signed cookies.
- ✅ `/play/:assetId` returns a manifest URL and sets cookies with a 1‑hour expiry.
- ✅ MediaConvert outputs in the correct folders; masters are written by the tools.
- ✅ Registry entry on-chain matches `provenanceCID`, `contentRootHash`, and `promptOriginHash`.
- ✅ EAS review + rating attestations visible for your `filmIdHash`.

That’s it. You can now upload → transcode → register → attest → stream, all on AWS with provenance anchored on-chain.

