package com.btd.maiscreen.api

import com.btd.maiscreen.auth.IdTokenProvider
import com.btd.maiscreen.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GenerateVideoRequest(
    val workflow: String,
    val prompt: String? = null,
    val duration: Int? = null,
    val resolution: String? = null,
    val aspectRatio: String? = null,
    val imageUrl: String? = null,
    val endImageUrl: String? = null,
    val videoUrl: String? = null,
    val imageUrls: List<String>? = null,
    val videoUrls: List<String>? = null,
    val audioUrls: List<String>? = null
)

@Serializable
data class GenerateVideoResponse(
    val requestId: String,
    val statusUrl: String
)

@Serializable
data class VideoStatusResponse(
    val requestId: String,
    val status: String,
    val videoUrl: String? = null,
    val error: String? = null
)

@Serializable
data class WebHandoffResponse(
    val handoffUrl: String
)

@Serializable
data class UserSettingsResponse(
    val generationProvider: String, // "enterprise" | "byok"
    val connected: Boolean,
    val minutesUsedThisMonth: Int,
    val minutesQuotaMonthly: Int
)

@Serializable
data class ConnectHiggsfieldKeyRequest(
    val keyId: String,
    val keySecret: String
)

@Serializable
data class GenerationProviderResponse(
    val generationProvider: String, // "enterprise" | "byok"
    val connected: Boolean
)

sealed class CreatorApiResult<out T> {
    data class Success<T>(val data: T) : CreatorApiResult<T>()
    data class Failure(val message: String) : CreatorApiResult<Nothing>()
}

/**
 * REST client for the Creator Suite's video-generation edge routes
 * (API Gateway behind CloudFront's /api/ path prefix), authenticated with the signed-in
 * user's Cognito ID token as a standard "Bearer" Authorization header —
 * unlike AppSyncClient, which sends the raw token with no prefix.
 *
 * The Higgsfield Seedance API key never reaches the client: these routes
 * proxy to Lambda functions that hold the key server-side.
 */
object CreatorApiClient {
    @PublishedApi
    internal val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun generateVideo(request: GenerateVideoRequest): CreatorApiResult<GenerateVideoResponse> {
        val idToken = IdTokenProvider.getIdToken()
            ?: return CreatorApiResult.Failure("Not signed in")

        return try {
            val response: GenerateVideoResponse = httpClient.post("${AppConfig.apiGatewayBaseUrl}/api/generate-video") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $idToken")
                setBody(request)
            }.body()
            CreatorApiResult.Success(response)
        } catch (e: Exception) {
            CreatorApiResult.Failure(e.message ?: "Network error calling generate-video")
        }
    }

    suspend fun getGenerationStatus(requestId: String): CreatorApiResult<VideoStatusResponse> {
        val idToken = IdTokenProvider.getIdToken()
            ?: return CreatorApiResult.Failure("Not signed in")

        return try {
            val response: VideoStatusResponse = httpClient.get("${AppConfig.apiGatewayBaseUrl}/api/generate-video/$requestId") {
                header("Authorization", "Bearer $idToken")
            }.body()
            CreatorApiResult.Success(response)
        } catch (e: Exception) {
            CreatorApiResult.Failure(e.message ?: "Network error calling generate-video status")
        }
    }

    /**
     * Mints a short-lived, single-use code the web Creator Suite can exchange
     * for a signed-in Cognito session, so opening the handoff URL in a browser
     * doesn't require the user to log in a second time.
     */
    suspend fun requestWebHandoff(): CreatorApiResult<WebHandoffResponse> {
        val idToken = IdTokenProvider.getIdToken()
            ?: return CreatorApiResult.Failure("Not signed in")

        return try {
            val response: WebHandoffResponse = httpClient.post("${AppConfig.apiGatewayBaseUrl}/api/web-handoff") {
                header("Authorization", "Bearer $idToken")
            }.body()
            CreatorApiResult.Success(response)
        } catch (e: Exception) {
            CreatorApiResult.Failure(e.message ?: "Network error calling web-handoff")
        }
    }

    /**
     * Current generation provider (shared enterprise key vs the user's own
     * connected Higgsfield account) and, if on the enterprise key, this
     * month's usage against the shared quota.
     */
    suspend fun getUserSettings(): CreatorApiResult<UserSettingsResponse> {
        val idToken = IdTokenProvider.getIdToken()
            ?: return CreatorApiResult.Failure("Not signed in")

        return try {
            val response: UserSettingsResponse = httpClient.get("${AppConfig.apiGatewayBaseUrl}/api/user-settings") {
                header("Authorization", "Bearer $idToken")
            }.body()
            CreatorApiResult.Success(response)
        } catch (e: Exception) {
            CreatorApiResult.Failure(e.message ?: "Network error calling user-settings")
        }
    }

    /**
     * Connects the caller's own Higgsfield account for generation, bypassing
     * the shared enterprise key's monthly quota. The key is encrypted
     * server-side immediately and never echoed back — only success/failure.
     */
    suspend fun connectHiggsfieldKey(keyId: String, keySecret: String): CreatorApiResult<GenerationProviderResponse> {
        val idToken = IdTokenProvider.getIdToken()
            ?: return CreatorApiResult.Failure("Not signed in")

        return try {
            val response: GenerationProviderResponse = httpClient.post("${AppConfig.apiGatewayBaseUrl}/api/user-settings/higgsfield-key") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $idToken")
                setBody(ConnectHiggsfieldKeyRequest(keyId, keySecret))
            }.body()
            CreatorApiResult.Success(response)
        } catch (e: Exception) {
            CreatorApiResult.Failure(e.message ?: "Network error connecting Higgsfield account")
        }
    }

    /** Reverts to the shared enterprise key and forgets the stored key. */
    suspend fun disconnectHiggsfieldKey(): CreatorApiResult<GenerationProviderResponse> {
        val idToken = IdTokenProvider.getIdToken()
            ?: return CreatorApiResult.Failure("Not signed in")

        return try {
            val response: GenerationProviderResponse = httpClient.delete("${AppConfig.apiGatewayBaseUrl}/api/user-settings/higgsfield-key") {
                header("Authorization", "Bearer $idToken")
            }.body()
            CreatorApiResult.Success(response)
        } catch (e: Exception) {
            CreatorApiResult.Failure(e.message ?: "Network error disconnecting Higgsfield account")
        }
    }
}
