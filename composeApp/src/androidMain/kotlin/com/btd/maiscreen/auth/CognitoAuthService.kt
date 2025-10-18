package com.btd.maiscreen.auth

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.btd.maiscreen.config.AppConfig
import net.openid.appauth.*
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.VersionedBrowserMatcher

/**
 * AWS Cognito Authentication Service using AppAuth
 * Handles OAuth2/OIDC flow with Cognito User Pools
 */
class CognitoAuthService(
    private val activity: ComponentActivity
) {
    companion object {
        private const val TAG = "CognitoAuthService"
        private const val INIT_AUTH = 1001
        private const val END_SESSION = 1002
    }

    private var authService: AuthorizationService? = null
    private var authState: net.openid.appauth.AuthState? = null
    private var cognitoServiceConfig: AuthorizationServiceConfiguration? = null
    
    // Callbacks
    var onAuthSuccess: ((TokenResponse) -> Unit)? = null
    var onAuthError: ((AuthorizationException) -> Unit)? = null
    var onLogoutComplete: (() -> Unit)? = null

    // Activity result launcher for authorization
    private val authLauncher: ActivityResultLauncher<Intent> = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleAuthorizationResult(result.data)
    }

    // Activity result launcher for logout
    private val logoutLauncher: ActivityResultLauncher<Intent> = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleLogoutResult(result.data)
    }

    init {
        initializeAuthService()
        fetchCognitoConfiguration()
    }

    private fun initializeAuthService() {
        // Create AuthService with browser configuration
        val browserMatcher = BrowserAllowList(
            VersionedBrowserMatcher.CHROME_CUSTOM_TAB,
            VersionedBrowserMatcher.SAMSUNG_CUSTOM_TAB
        )
        
        authService = AuthorizationService(
            activity,
            AppAuthConfiguration.Builder()
                .setBrowserMatcher(browserMatcher)
                .build()
        )
        
        Log.d(TAG, "✅ AuthService initialized")
    }

    private fun fetchCognitoConfiguration() {
        Log.d(TAG, "🔄 Fetching Cognito configuration from: ${AppConfig.cognitoIssuerUri}")
        
        AuthorizationServiceConfiguration.fetchFromIssuer(
            Uri.parse(AppConfig.cognitoIssuerUri)
        ) { config, ex ->
            if (ex != null) {
                Log.e(TAG, "❌ Failed to fetch Cognito service config", ex)
                onAuthError?.invoke(ex)
                return@fetchFromIssuer
            }
            
            cognitoServiceConfig = config
            Log.d(TAG, "✅ Cognito service configuration retrieved")
            Log.d(TAG, "Authorization endpoint: ${config?.authorizationEndpoint}")
            Log.d(TAG, "Token endpoint: ${config?.tokenEndpoint}")
        }
    }

    /**
     * Start the OAuth2 authorization flow
     */
    fun startAuthorization() {
        val config = cognitoServiceConfig
        if (config == null) {
            Log.e(TAG, "❌ Cognito configuration not available")
            onAuthError?.invoke(AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW)
            return
        }

        Log.d(TAG, "🚀 Starting authorization flow")
        
        val authRequest = AuthorizationRequest.Builder(
            config,
            AppConfig.cognitoClientId,
            ResponseTypeValues.CODE,
            Uri.parse(AppConfig.cognitoRedirectUri)
        )
            .setScope(AppConfig.cognitoScope)
            .build()

        Log.d(TAG, "📋 Auth request details:")
        Log.d(TAG, "  Client ID: ${authRequest.clientId}")
        Log.d(TAG, "  Redirect URI: ${authRequest.redirectUri}")
        Log.d(TAG, "  Scope: ${authRequest.scope}")

        val authIntent = authService?.getAuthorizationRequestIntent(authRequest)
        if (authIntent != null) {
            authLauncher.launch(authIntent)
        } else {
            Log.e(TAG, "❌ Failed to create auth intent")
            onAuthError?.invoke(AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW)
        }
    }

    private fun handleAuthorizationResult(data: Intent?) {
        if (data == null) {
            Log.e(TAG, "❌ No data received from authorization")
            onAuthError?.invoke(AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW)
            return
        }

        val response = AuthorizationResponse.fromIntent(data)
        val ex = AuthorizationException.fromIntent(data)

        if (ex != null) {
            Log.e(TAG, "❌ Authorization failed", ex)
            onAuthError?.invoke(ex)
            return
        }

        if (response != null) {
            Log.d(TAG, "✅ Authorization successful, exchanging code for tokens")
            
            // Update auth state
            authState = net.openid.appauth.AuthState(response, ex)
            
            // Exchange authorization code for tokens
            authService?.performTokenRequest(
                response.createTokenExchangeRequest()
            ) { tokenResponse, tokenEx ->
                if (tokenEx != null) {
                    Log.e(TAG, "❌ Token exchange failed", tokenEx)
                    onAuthError?.invoke(tokenEx)
                    return@performTokenRequest
                }

                if (tokenResponse != null) {
                    Log.d(TAG, "✅ Token exchange successful")
                    Log.d(TAG, "Access token: ${tokenResponse.accessToken?.substring(0, 20)}...")
                    Log.d(TAG, "ID token: ${tokenResponse.idToken?.substring(0, 20)}...")
                    
                    // Update auth state with tokens
                    authState?.update(tokenResponse, tokenEx)
                    
                    onAuthSuccess?.invoke(tokenResponse)
                } else {
                    Log.e(TAG, "❌ No token response received")
                    onAuthError?.invoke(AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW)
                }
            }
        } else {
            Log.e(TAG, "❌ No authorization response received")
            onAuthError?.invoke(AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW)
        }
    }

    /**
     * Start the logout/end session flow
     */
    fun logout() {
        val config = cognitoServiceConfig
        val currentAuthState = authState
        
        if (config == null || currentAuthState == null) {
            Log.w(TAG, "⚠️ No config or auth state available for logout, clearing local state")
            clearAuthState()
            onLogoutComplete?.invoke()
            return
        }

        // Get ID token from the auth state
        val idToken = currentAuthState.idToken
        if (idToken == null) {
            Log.w(TAG, "⚠️ No ID token available for logout, clearing local state")
            clearAuthState()
            onLogoutComplete?.invoke()
            return
        }

        Log.d(TAG, "🚪 Starting logout flow")

        val endSessionRequest = EndSessionRequest.Builder(config)
            .setIdTokenHint(idToken)
            .setPostLogoutRedirectUri(Uri.parse(AppConfig.cognitoLogoutUri))
            .setAdditionalParameters(mapOf(
                "client_id" to AppConfig.cognitoClientId,
                "logout_uri" to AppConfig.cognitoLogoutUri
            ))
            .build()

        val endSessionIntent = authService?.getEndSessionRequestIntent(endSessionRequest)
        if (endSessionIntent != null) {
            logoutLauncher.launch(endSessionIntent)
        } else {
            Log.e(TAG, "❌ Failed to create logout intent")
            clearAuthState()
            onLogoutComplete?.invoke()
        }
    }

    private fun handleLogoutResult(data: Intent?) {
        val response = EndSessionResponse.fromIntent(data ?: Intent())
        val ex = AuthorizationException.fromIntent(data)

        if (ex != null) {
            Log.e(TAG, "❌ Logout failed", ex)
        } else {
            Log.d(TAG, "✅ Logout successful")
        }

        clearAuthState()
        onLogoutComplete?.invoke()
    }

    private fun clearAuthState() {
        authState = null
        Log.d(TAG, "🧹 Auth state cleared")
    }

    /**
     * Get the current access token if available
     */
    fun getAccessToken(): String? {
        return authState?.accessToken
    }

    /**
     * Get the current ID token if available
     */
    fun getIdToken(): String? {
        return authState?.idToken
    }

    /**
     * Check if user is currently authenticated
     */
    fun isAuthenticated(): Boolean {
        return authState?.isAuthorized == true
    }

    /**
     * Clean up resources
     */
    fun dispose() {
        authService?.dispose()
        authService = null
        Log.d(TAG, "🧹 CognitoAuthService disposed")
    }
}