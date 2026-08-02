package com.nullo.voidapp.core.auth.data.service

import com.nullo.voidapp.core.auth.data.dto.ExchangeCodeRequest
import com.nullo.voidapp.core.auth.data.dto.ExchangeCodeResponse
import com.nullo.voidapp.core.auth.domain.exception.AuthException
import com.nullo.voidapp.core.utils.kotlin.withBearerPrefix
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

    override suspend fun exchangeCode(code: String?, codeVerifier: String): String {
        if (code == null) throw AuthException.CodeExchangeException()

        val response = try {
            httpClient.post(CODE_EXCHANGE_ENDPOINT) {
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
            throw AuthException.CodeExchangeException(e)
        }
        return response.apiKey
    }

    override suspend fun validateApiKey(apiKey: String) {
        val response = try {
            httpClient.get(CHECK_API_KEY_ENDPOINT) {
                header(HttpHeaders.Authorization, apiKey.withBearerPrefix())
                expectSuccess = false
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw AuthException.ValidationNetworkException(e)
        }

        when (response.status) {
            HttpStatusCode.OK -> return
            HttpStatusCode.Unauthorized -> throw AuthException.InvalidApiKeyException()
            HttpStatusCode.InternalServerError -> throw AuthException.InternalServerException()
            else -> throw AuthException.UnexpectedStatusException()
        }
    }

    companion object {

        const val CODE_EXCHANGE_ENDPOINT = "auth/keys"
        const val CHECK_API_KEY_ENDPOINT = "key"
    }
}
