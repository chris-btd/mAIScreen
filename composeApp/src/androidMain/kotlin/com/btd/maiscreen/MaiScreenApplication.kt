package com.btd.maiscreen

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify

/**
 * Application class to initialize AWS Amplify
 */
class MaiScreenApplication : Application() {
    
    companion object {
        private const val TAG = "MaiScreenApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Add the Cognito Auth plugin
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            
            // Configure Amplify with the configuration file
            Amplify.configure(com.amplifyframework.core.AmplifyConfiguration.fromConfigFile(applicationContext, com.btd.maiscreen.R.raw.awsconfiguration), applicationContext)
            
            Log.i(TAG, "✅ Initialized Amplify successfully")
            Log.i(TAG, "📋 Cognito issuer: ${com.btd.maiscreen.config.AppConfig.cognitoIssuerUri}")
            Log.i(TAG, "📋 App Client ID: ${com.btd.maiscreen.config.AppConfig.cognitoClientId}")
            
            // Check current auth state
            Amplify.Auth.fetchAuthSession(
                { result ->
                    Log.i(TAG, "🔐 Auth session: ${result.isSignedIn}")
                },
                { error ->
                    Log.w(TAG, "⚠️ Failed to fetch auth session: ${error.message}", error)
                }
            )
            
        } catch (error: AmplifyException) {
            Log.e(TAG, "❌ Could not initialize Amplify: ${error.message}", error)
            Log.e(TAG, "❌ Error details: ${error.cause}")
        }
    }
}
