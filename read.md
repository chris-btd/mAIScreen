# AI Film Platform — Product & Technical Blueprint

## 1) Elevator Pitch

A cross‑platform hub where creators publish AI‑generated anime, animated series, and films. Each work is immutably registered on‑chain with its creative provenance: prompts, models/tools used, generation parameters, and authorship. Viewers can stream, tip, subscribe, collect editions, and license usage. Built with **Kotlin Multiplatform (iOS/Android/Desktop)** and **React (web)**, backed by a crypto‑native content graph and decentralized storage.

---

## 2) Core Objectives

* **Provenance & Ownership:** On‑chain minting of a “Provenance NFT” (ERC‑721/1155) that references metadata (prompts, model/tool versions, seed, parameters, source thumbnails) and content hashes stored on IPFS/Arweave.
* **Publish & Stream:** Upload, transcode, and stream AI films with adaptive bitrate (HLS/DASH).
* **Monetize:** Editions, pay‑per‑view, subscriptions, tipping, and licensing.
* **Community:** Follows, comments, curated playlists, contests.
* **Compliance & Safety:** Model/tool disclosure, copyright checks, moderation workflows.

---

## 3) User Roles

* **Creator:** Uploads media, mints provenance token, sets editions/licensing, manages storefront.
* **Collector/Fan:** Streams, tips, collects editions, licenses clips for derivative works.
* **Curator:** Creates playlists/anthologies; can receive rev‑share.
* **Reviewer/Moderator:** Reviews flags, handles takedowns, appeals.
* **Org/Studio:** Team spaces, revenue splits, API access.

---

## 4) High‑Level Architecture

* **Clients:**

    * **KMP app:** Compose Multiplatform UI (Android/iOS), shared domain/data layers.
    * **Web:** React + Next.js (SSR/ISR), Tailwind, shadcn/ui.
* **Backend:**

    * **Core API:** Kotlin Ktor or Node/NestJS (GraphQL or REST) for auth, uploads, catalog, payments, moderation.
    * **Jobs/Workers:** Transcoding (FFmpeg/MediaConvert), thumbnail/sprite sheets, perceptual hashing.
    * **Storage:**

        * Media: S3 (origin) + CloudFront CDN; archival to IPFS/Arweave via Pinata/Web3.Storage.
        * Metadata: Postgres (core), Redis (cache), on‑chain refs to IPFS CIDs.
* **Web3:**

    * EVM (Polygon/BASE) for minting; contract kit + relayer for gas‑abstracted UX.
    * Sign‑in with wallet (SIWE) + custodial fallback (email/passkey) via Web3Auth/Privy.
* **Observability:** OpenTelemetry, Grafana, Sentry.

---

## 5) Data & Provenance Model (Off‑chain + On‑chain)

**Film**

* `id`, `title`, `synopsis`, `runtimeSec`, `ageRating`, `tags[]`, `language`, `coverImageCid`, `trailerCid`, `hlsManifestUrl`
* `hashes`: `blake3`, `md5`, `perceptualHash` (pHash/dHash)
* `creatorId`, `createdAt`, `publishedAt`, `status`

**Provenance**

* `filmId`
* `prompts[]` (structured JSON; multi‑prompt timelines)
* `models[]` (e.g., `{name:"ComfyUI+Veo", version:"3.0", checkpoint:"…", loras:[…], samplers:[…], seeds:[…], cfgScale, steps}`)
* `tools[]` (audio/voice/cloning, post‑FX)
* `datasetRefs[]` (if any), `citations[]`
* `renderPipelineGraph` (DAG JSON), `renderTime`, `gpuInfo`
* `attestations[]` (signatures by creator, platform, model provider)

**Editions**

* `tokenStandard` (ERC‑721/1155)
* `maxSupply`, `royaltyBps`, `primaryPrice`, `secondaryRoyaltyReceiver`
* `license` (standardized SPDX‑like: personal, commercial, remixable)

**Moderation**

* `status` (pending/manual/blocked), `flags[]`, `takedownNotices[]`, `appeals[]`

**On‑chain Metadata JSON (IPFS)**

* `name`, `description`, `image` (poster), `animation_url` (trailer or stream entry),
* `external_url`, `attributes`: provenance digest, model/tool names, runtime, rating, language
* `provenance_cid`, `content_hashes`, `license`, `editions`

---

## 6) Smart Contract Design

* **Registry (Upgradeable):** Maps `filmId → tokenContract, tokenIdRange, provenanceCID`.
* **Edition Contract:** Minimal ERC‑1155 for scalable editions; EIP‑2981 royalties; EIP‑712 signatures for lazy minting; operator filtering.
* **Minting Flow:**

    1. Upload media → get CIDs + content hashes.
    2. Build metadata JSON, commit to IPFS → get `provenanceCID`.
    3. `createEdition(provenanceCID, supply, price, royaltyBps)` → emits event.
    4. Relayer handles user mints (gasless) or user signs tx (SIWE).
* **Attestations:** Optional EAS (Ethereum Attestation Service) to sign credibility claims (e.g., “created with Model X”).

---

## 7) Storage & Streaming Strategy

* **Upload** → S3 multipart with pre‑signed URLs (client never sees credentials).
* **Transcode** → HLS variants (240p–4K), audio normalization, subtitle ingest.
* **CDN** → CloudFront/Cloudflare; token‑based signed URLs for paid content.
* **Archival** → Periodic IPFS/Arweave pinning; keep CIDs in DB and on‑chain.

---

## 8) Payments & Monetization

* **Fiat:** Stripe for tips, subscriptions, PPV; Connect for creator payouts.
* **Crypto:** Stablecoin payments on L2 (USDC); on‑chain mints/purchases.
* **Revenue Splits:** Contract‑level splitters or platform ledger distributing to multiple wallets.
* **Licensing:** SKU‑ed licenses (clip packs, soundtrack) with watermark/unlock keys.

---

## 9) Safety, Legal & Policy

* **Copyright checks:** Fingerprinting (pHash), AI‑dataset disclosure, known‑work match.
* **Content rules:** No CSAM, hate, extreme violence; age gates; geographic locks if required.
* **DMCA:** Notice/takedown and counter‑notice workflow; repeat‑infringer policy.
* **Right of Publicity:** Face/voice cloning labels; consent receipts.

---

## 10) KMP App Architecture

* **Modules:** `:shared` (domain, data), `:androidApp`, `:iosApp`.
* **Shared:** Kotlinx Serialization, Ktor Client, SQLDelight, Coroutines/Flow, Koin or KMP‑Hilt.
* **Features:** Auth (wallet/passkey), Uploads (pre‑signed), Player (ExoPlayer/AVKit wrappers), Wallet (Web3 modal), Collect (NFT), Profile, Feed, Search.
* **Offline:** Local cache of catalog, optimistic actions; background uploads.

---

## 11) Web (React) Architecture

* **Next.js 15 (App Router)**, RSC for data fetching, Zod schemas, React Query for client caching.
* **UI:** Tailwind + shadcn/ui; upload surface; player with HLS.js; Web3 modal.
* **Auth:** SIWE + email/passkey fallback; session via httpOnly cookies.

---

## 12) API Surface (Illustrative)

**POST /v1/uploads/init** → `{uploadId, presignedParts[]}`
**POST /v1/films** → create film draft
**POST /v1/films/{id}/provenance**
**POST /v1/editions** → init mint (returns checkout link or relayed tx)
**GET /v1/films/{id}** → details + signed HLS URL
**POST /v1/moderation/flags**

---

## 13) Security & Trust

* **Content integrity:** Store BLAKE3 hash of original + mezzanine. Compare post‑transcode.
* **Provenance digest:** `sha256(canonical_provenance_json)`; commit hash on‑chain.
* **Watermarking:** Invisible watermark for licensed copies; forensic markers per purchase.
* **Key mgmt:** KMS for server keys; MPC/custodial wallets for newbies; never custody user seed phrases in plaintext.

---

## 14) Analytics & Growth

* **KPIs:** MAU, uploads, watch time, mint conversions, ARPU, creator LTV.
* **Discovery:** Hashtags, model tags, challenges (“48‑hour short”), featured curators.
* **SEO:** Dynamic OG images, sitemaps, structured data for films/episodes.

---

## 15) Roadmap (Phased)

**Phase 0 – Spike (2–3 wks):**

* Upload → HLS → play on web; IPFS pin; simple ERC‑1155 edition mint on testnet.

**Phase 1 – Alpha (6–8 wks):**

* KMP app MVP (feed, watch, upload), creator dashboard, gasless mint via relayer, Stripe tips, moderation queue.

**Phase 2 – Beta (8–12 wks):**

* Subscriptions, playlists, curators, licensing SKUs, forensic watermarking, attestation integration.

**Phase 3 – GA:**

* iOS/Android store release, marketplace features, studio/teams, analytics, governance hooks.

---

## 16) Naming & Brand Starters

* **SteadFilms**, **Overstand Studio**, **FrameMint**, **CineProve**, **PromptReel**.

---

## 17) Example Artifacts (Skeletons)

**Solidity (Edition ERC‑1155 skeleton)**

```solidity
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;
import "@openzeppelin/contracts/token/ERC1155/ERC1155.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/token/common/ERC2981.sol";

contract FilmEditions is ERC1155, ERC2981, Ownable {
    struct Edition { uint256 price; uint256 supply; uint256 minted; string uri; }
    mapping(uint256 => Edition) public editions; // tokenId→Edition
    constructor() ERC1155("") {}
    function createEdition(uint256 id, uint256 price, uint256 supply, string calldata uri, address royalty, uint96 bps) external onlyOwner {
        editions[id] = Edition(price, supply, 0, uri);
        _setTokenRoyalty(id, royalty, bps);
    }
    function mint(address to, uint256 id, uint256 amount) external payable {
        Edition storage e = editions[id];
        require(e.minted + amount <= e.supply, "sold out");
        require(msg.value == e.price * amount, "price");
        e.minted += amount; _mint(to, id, amount, "");
    }
    function uri(uint256 id) public view override returns (string memory) { return editions[id].uri; }
    function supportsInterface(bytes4 i) public view override(ERC1155, ERC2981) returns (bool) { return super.supportsInterface(i); }
}
```

**Provenance JSON (canonicalized)**

```json
{
  "filmId": "flm_01H...",
  "prompts": [{"t":0,"text":"city night, rain, neon, anime style"}],
  "models": [{"name":"Veo","version":"3.0","sampler":"ddim","seed":42,"cfg":7.5}],
  "tools": ["ComfyUI","Audacity"],
  "pipelineDAG": {"nodes":[],"edges":[]},
  "gpu": {"make":"NVIDIA","model":"A100"},
  "contentHashes": {"blake3":"…"}
}
```

**KMP Shared Data Model (Kotlin)**

```kotlin
@Serializable data class Film(
  val id: String, val title: String, val synopsis: String? = null,
  val runtimeSec: Int, val coverCid: String?, val hlsUrl: String?,
  val tags: List<String> = emptyList(), val creatorId: String,
)

@Serializable data class Provenance(
  val filmId: String, val prompts: List<Prompt>, val models: List<ModelSpec>,
  val pipelineDag: String, val contentHashes: Map<String, String>
)
```

**Next.js Upload→Mint Flow (pseudo)**

```ts
// 1) Request presigned upload
// 2) Multipart upload file
// 3) Trigger transcode job
// 4) Pin metadata to IPFS, get CID
// 5) Call backend to create edition (relayer mints)
```

---

## 18) Open Questions

* Model‑provider terms (e.g., training data disclosures) required at publish time?
* Which L2 chain first (Polygon vs Base) and custody strategy for best UX?
* What moderation automation threshold (hash‑match + human review) is acceptable?
* How to handle copyrighted voices/faces (consent receipts)?

---

## 19) First Sprint Backlog (Concrete)

* [ ] Back‑end project scaffold (Ktor/Nest), auth, DB migrations.
* [ ] S3 multipart upload + webhook to start transcode job.
* [ ] HLS playback on web, basic film detail page.
* [ ] IPFS pinning util (Pinata/Web3.Storage) + CID return.
* [ ] Testnet ERC‑1155 deploy + mint from relayer.
* [ ] KMP shared module skeleton + feed screen pulling mock films.
