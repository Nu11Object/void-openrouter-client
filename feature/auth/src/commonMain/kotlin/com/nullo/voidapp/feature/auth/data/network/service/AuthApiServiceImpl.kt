package com.nullo.voidapp.feature.auth.data.network.service

import com.nullo.voidapp.core.utils.kotlin.withBearerPrefix
import com.nullo.voidapp.feature.auth.data.network.AuthConfig
import com.nullo.voidapp.feature.auth.data.network.dto.ExchangeCodeRequest
import com.nullo.voidapp.feature.auth.data.network.dto.ExchangeCodeResponse
import com.nullo.voidapp.feature.auth.domain.entity.InternalServerException
import com.nullo.voidapp.feature.auth.domain.entity.InvalidApiKeyException
import com.nullo.voidapp.feature.auth.domain.entity.OAuthException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlin.coroutines.cancellation.CancellationException

internal class AuthApiServiceImpl(
    private val httpClient: HttpClient
) : AuthApiService {

    override suspend fun exchangeCode(code: String, codeVerifier: String): String {
        val response = try {
            httpClient.post(AuthConfig.CODE_EXCHANGE_ENDPOINT) {
                setBody(
                    ExchangeCodeRequest(
                        code = code,
                        codeVerifier = codeVerifier,
                        codeChallengeMethod = "S256",
                    )
                )
            }.body<ExchangeCodeResponse>()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw OAuthException(
                "The authorization code exchange failed. " +
                        "Please try again or login with API key instead.",
                cause = e
            )
        }
        return response.apiKey
    }

    override suspend fun validateApiKey(apiKey: String) {
        val response = try {
            httpClient.get(AuthConfig.CHECK_API_KEY_ENDPOINT) {
                header(HttpHeaders.Authorization, apiKey.withBearerPrefix())
                expectSuccess = false
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw Exception(
                "An error occurred during API key validation. " +
                        "Please check your internet connection and ensure the key is valid.",
                e
            )
        }

        when (response.status) {
            HttpStatusCode.OK -> return
            HttpStatusCode.Unauthorized -> throw InvalidApiKeyException()
            HttpStatusCode.InternalServerError -> throw InternalServerException()
            else -> throw Exception("Api key validation failed with status: ${response.status}")
        }
    }
}
