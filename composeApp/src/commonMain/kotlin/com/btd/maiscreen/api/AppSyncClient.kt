package com.btd.maiscreen.api

import com.btd.maiscreen.auth.IdTokenProvider
import com.btd.maiscreen.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

@Serializable
data class GraphQLRequest(
    val query: String,
    val variables: JsonObject = JsonObject(emptyMap())
)

@Serializable
data class GraphQLError(val message: String)

@Serializable
data class GraphQLEnvelope<T>(
    val data: T? = null,
    val errors: List<GraphQLError>? = null
)

sealed class AppSyncResult<out T> {
    data class Success<T>(val data: T) : AppSyncResult<T>()
    data class Failure(val message: String) : AppSyncResult<Nothing>()
}

/**
 * Minimal GraphQL client for AppSync, authenticated with the signed-in user's
 * Cognito ID token — matches the AppSync API's "Amazon Cognito User Pools"
 * auth mode set up for this project.
 *
 * Note: AppSync's Cognito User Pools auth mode expects the raw ID token as the
 * `Authorization` header value directly (no "Bearer " prefix) — unlike the
 * API Gateway routes elsewhere in this app.
 */
object AppSyncClient {
    @PublishedApi
    internal val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend inline fun <reified T> query(
        query: String,
        variables: JsonObject = JsonObject(emptyMap())
    ): AppSyncResult<T> {
        val idToken = IdTokenProvider.getIdToken()
            ?: return AppSyncResult.Failure("Not signed in")

        return try {
            val envelope: GraphQLEnvelope<T> = httpClient.post(AppConfig.appSyncEndpoint) {
                contentType(ContentType.Application.Json)
                header("Authorization", idToken)
                setBody(GraphQLRequest(query, variables))
            }.body()

            val errors = envelope.errors
            when {
                !errors.isNullOrEmpty() -> AppSyncResult.Failure(errors.joinToString("; ") { it.message })
                envelope.data != null -> AppSyncResult.Success(envelope.data)
                else -> AppSyncResult.Failure("Empty response from AppSync")
            }
        } catch (e: Exception) {
            AppSyncResult.Failure(e.message ?: "Network error calling AppSync")
        }
    }
}
