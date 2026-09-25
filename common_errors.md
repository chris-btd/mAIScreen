# Common Errors

## API Gateway HTTP API console: "Configure CORS" writes Allow-Headers into the wrong field

**Symptoms**
- You configure CORS in the console (API Gateway → your HTTP API → CORS), filling in
  `Access-Control-Allow-Origin`, `Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`,
  and clicking **Save**.
- The read-only CORS summary page shows all the values you entered, looking correct.
- Browser requests still fail with `Failed to fetch` / a CORS error in devtools.
- `curl -X OPTIONS <route> -H "Origin: ..." -H "Access-Control-Request-Method: ..." -H
  "Access-Control-Request-Headers: authorization,content-type"` returns `204` but with **zero**
  `Access-Control-*` response headers — even after saving multiple times, waiting for
  propagation, and confirming there's no conflicting manually-created `OPTIONS` route on the
  path (API Gateway HTTP APIs handle CORS preflight automatically per-API, not per-route).

**Root cause**
- The console's CORS editor silently wrote the "Access-Control-Allow-Headers" value into the
  API's `ExposeHeaders` field instead of `AllowHeaders`. Confirmed by inspecting the live API
  object directly:
  ```
  aws apigatewayv2 get-api --api-id <api-id>
  ```
  `CorsConfiguration.AllowHeaders` was **absent entirely**; the value that should have been
  there (`content-type`, `authorization`) had ended up under `CorsConfiguration.ExposeHeaders`.
- Without `AllowHeaders` set, API Gateway can't confirm the browser's requested headers
  (sent via `Access-Control-Request-Headers` on the preflight) are allowed, so it omits all
  `Access-Control-*` headers from its response rather than erroring — the OPTIONS request
  still returns a bare `204`, which makes this look like it "should" be working.
- Re-saving the same form through the console again does **not** fix it — the bug is in how
  the console maps the form to the underlying API update, not a one-off save glitch.

**Fix**
Bypass the console entirely and set the CORS config directly via CLI (CloudShell works, no
local AWS CLI setup needed — click the `>_` icon in the AWS Console top nav):
```bash
aws apigatewayv2 update-api --api-id <api-id> --cors-configuration '{
  "AllowOrigins": ["http://localhost:3000"],
  "AllowMethods": ["GET","POST","DELETE","OPTIONS"],
  "AllowHeaders": ["content-type","authorization"],
  "ExposeHeaders": [],
  "MaxAge": 0,
  "AllowCredentials": false
}'
```
Adjust `AllowOrigins`/`AllowMethods` for your actual routes. This both sets `AllowHeaders`
correctly and clears the misplaced `ExposeHeaders` value back to empty (which is what you
want unless you specifically need JS to read a custom response header).

**Verify**
```bash
curl -i -X OPTIONS <route> \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: authorization,content-type"
```
✅ Fixed: response includes `access-control-allow-origin`, `access-control-allow-methods`,
and `access-control-allow-headers`.
❌ Still broken: re-run `aws apigatewayv2 get-api --api-id <api-id>` and confirm
`CorsConfiguration.AllowHeaders` actually contains what you expect — don't trust the console's
summary page, it can show the "right" values while the underlying API object is wrong.

---

## CloudFront signed cookies: Public key pasted or PEM lost newlines

**Symptoms**
- Lambda returns `500 Error: error:1E08010C:DECODER routines::unsupported` when signing.
- No `Set-Cookie` headers for `CloudFront-Policy / CloudFront-Signature / CloudFront-Key-Pair-Id`.
- Logs show `asymmetricKeyType` never prints or the signer throws on `sign(...)`.

**Root cause**
- The value in `CF_PRIVATE_KEY` (or the secret) is **not a private key** (you accidentally pasted a `-----BEGIN PUBLIC KEY-----`).
- Or the private key PEM lost **required newlines**, especially the newline *after* the `BEGIN …` header and *before* the `END …` footer (common when storing as a single line with `\n`).

**What CloudFront expects**
- Lambda must sign with a **PRIVATE** RSA key (PKCS#8 `BEGIN PRIVATE KEY` or PKCS#1 `BEGIN RSA PRIVATE KEY`).
- CloudFront stores only the **PUBLIC** key in a **Key Group**. Your `/assets/*` behavior trusts that key group.

---

## Fix

1) **Use a real private key**
   - Private key (for Lambda):
     ```text
     -----BEGIN PRIVATE KEY-----
     ...
     -----END PRIVATE KEY-----
     ```
     or
     ```text
     -----BEGIN RSA PRIVATE KEY-----
     ...
     -----END RSA PRIVATE KEY-----
     ```
   - Public key (for CloudFront → Security → Public keys):
     ```text
     -----BEGIN PUBLIC KEY-----
     ...
     -----END PUBLIC KEY-----
     ```

2) **Normalize newlines & strip accidental quoting**
   Paste this helper and use it to load the key from env/Secrets Manager:

   ```js
   // normalizePem: fixes \n -> real newlines, removes CRLF, enforces header/footer breaks,
   // and guards against mistakenly pasting a PUBLIC key.
   function normalizePem(input) {
     let k = String(input).trim();
     if (k.startsWith('"') && k.endsWith('"')) k = k.slice(1, -1); // strip quotes
     k = k.replace(/\r/g, "");        // CRLF -> LF
     k = k.replace(/\\n/g, "\n");    // literal \n -> newline
     // ensure newline after header and before footer
     k = k.replace(/-----BEGIN ([A-Z ]+)-----\s*/g, '-----BEGIN $1-----\n');
     k = k.replace(/\s*-----END ([A-Z ]+)-----/g, '\n-----END $1-----');
     if (!k.endsWith('\n')) k += '\n';
     if (k.includes('-----BEGIN PUBLIC KEY-----')) {
       throw new Error('CF_PRIVATE_KEY is a PUBLIC key. Use the PRIVATE key for signing.');
     }
     return k;
   }
   ```

3) **Parse to a KeyObject (OpenSSL 3 friendly) and sign**

   ```js
   import crypto from 'crypto';

   const pem = normalizePem(process.env.CF_PRIVATE_KEY /* or secret value */);
   const type = pem.includes('BEGIN RSA PRIVATE KEY') ? 'pkcs1' : 'pkcs8';
   const keyObj = crypto.createPrivateKey({ key: pem, format: 'pem', type });

   const policyJson = JSON.stringify({
     Statement: [{
       Resource: `https://${process.env.CF_DOMAIN}/assets/${assetId}/*`,
       Condition: { DateLessThan: { 'AWS:EpochTime': expires } }
     }]
   });

   function b64Safe(buf) {
     return Buffer.from(buf).toString('base64')
       .replace(/=+$/g, '_').replace(/\+/g, '-').replace(/\//g, '~');
   }

   const signer = crypto.createSign('RSA-SHA1'); // CloudFront cookie requirement
   signer.update(policyJson);
   signer.end();
   const signatureB64 = b64Safe(signer.sign(keyObj));
   const policyB64 = b64Safe(policyJson);

   const attrs = 'Path=/; Secure; HttpOnly; SameSite=Lax';
   const cookies = [
     `CloudFront-Policy=${policyB64}; ${attrs}`,
     `CloudFront-Signature=${signatureB64}; ${attrs}`,
     `CloudFront-Key-Pair-Id=${process.env.CF_KEY_PAIR_ID}; ${attrs}`,
   ];
   ```

4) **Sanity checks** (log once while testing)

   ```js
   console.log('PEM first line:', pem.split('\n')[0]);        // BEGIN PRIVATE KEY or BEGIN RSA PRIVATE KEY
   console.log('asymmetricKeyType:', keyObj.asymmetricKeyType); // 'rsa'
   console.log('policy.len:', policyJson.length);
   ```

---

## Quick regeneration commands

Generate a fresh unencrypted RSA key (PKCS#8) and derive public key:

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out cf-private.pem
openssl pkey   -in cf-private.pem -pubout -out cf-public.pem
```

If you already have PKCS#1 and want PKCS#8:

```bash
openssl pkcs8 -topk8 -nocrypt -in cf-private.pkcs1.pem -out cf-private.pem
```

---

## Typical outcomes
- ✅ After fix: Lambda returns `200` with **3 Set-Cookie headers** and a valid `manifestUrl`.
- ❌ Still failing: Double‑check the `/assets/*` behavior trusts your Key Group and the `CF_KEY_PAIR_ID` matches the uploaded public key.
