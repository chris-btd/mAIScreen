# Debug Snippets

Temporary logging snippets used to pull real credentials out of a running mobile app for
manual backend testing (curl / Postman / browser devtools). None of these should be left in
committed code — copy them back in when you need them, then remove them again afterward.

---

## Get a real Cognito ID token

Prints the signed-in user's ID token to Logcat right after a normal sign-in completes. Use
this to grab a Bearer token for testing API Gateway routes directly (curl, etc.) without
needing a full mobile-to-web handoff.

**Where:** `composeApp/src/androidMain/kotlin/com/btd/maiscreen/auth/AmplifyAuthService.kt`,
inside `signIn()`'s success callback, right after `if (result.isSignedIn) {`:

```kotlin
                    // TEMPORARY - for pulling a real ID token to test API Gateway routes via
                    // curl. Remove this block once backend testing is done.
                    getIdToken { token ->
                        Log.d(TAG, "🪪 TEST_ID_TOKEN: $token")
                    }
```

**How to use:**
1. Add the snippet, rebuild, log in through the app's normal LoginScreen.
2. Filter Logcat for `TEST_ID_TOKEN`.
3. Copy everything after the colon — that's your Bearer token.
4. Tokens expire in ~1 hour (`exp` claim). Decode without a library:
   ```bash
   python3 -c "
   import base64, json, time
   payload = '<token>'.split('.')[1]
   payload += '=' * (-len(payload) % 4)
   d = json.loads(base64.urlsafe_b64decode(payload))
   print('exp:', d['exp'], 'now:', int(time.time()), 'expired:', int(time.time()) > d['exp'])
   "
   ```

**To also test the web app with a real session** (skips the web-handoff flow entirely — only
works if `handoffUrl`/CUSTOM_AUTH infra isn't set up, or you just want a fast shortcut): open
the web app in a browser, then in devtools console:
```js
sessionStorage.setItem('mai_creator_suite_session', JSON.stringify({
  idToken: 'PASTE_TOKEN_HERE',
  accessToken: '',
  refreshToken: ''
}));
location.reload();
```
(Key/shape defined in `AIFilmtudio/src/lib/auth.ts` — `getIdToken()` is the only field the app
actually sends anywhere; `accessToken`/`refreshToken` can be empty strings for this shortcut.)

---

## Get the mobile-to-web handoff URL

Prints the handoff URL (with its short-lived, single-use code) before the app tries to open it
in the emulator's own internal browser — useful when you want to open it in a *real* desktop
browser instead (e.g. testing on an Android emulator, where the emulator's browser can't reach
your host machine's dev server the way you'd want).

**Where:** `composeApp/src/commonMain/kotlin/com/btd/maiscreen/ui/screens/CreatorSuiteScreen.kt`,
in the "Open Creator Suite" button's `onClick`, right before `uriHandler.openUri(...)`:

```kotlin
                                is CreatorApiResult.Success -> {
                                    isLoading = false
                                    println("HANDOFF_URL_DEBUG: " + result.data.handoffUrl) // TEMPORARY - remove after testing
                                    uriHandler.openUri(result.data.handoffUrl)
                                }
```

**How to use:**
1. Add the snippet, rebuild, filter Logcat for `HANDOFF_URL_DEBUG` *before* tapping the button
   (codes expire in 60 seconds — have Logcat ready first).
2. Tap "Open Creator Suite" — ignore whatever opens in the emulator's own browser.
3. Immediately grab the `code=` value from the logged URL.
4. Open `http://localhost:3000/auth/handoff?code=<code>` (or wherever your web app is running)
   in your actual browser, immediately, before the 60s window closes.

Note: this route (`/api/web-handoff`) requires the full `CUSTOM_AUTH` Cognito setup described
in the "Required Setup for Mobile-to-Web SSO Handoff" section of
`lambda-environment-variables.md` — if that infra isn't deployed, this will fail with a 404 and
the sessionStorage shortcut above is the faster path anyway.
