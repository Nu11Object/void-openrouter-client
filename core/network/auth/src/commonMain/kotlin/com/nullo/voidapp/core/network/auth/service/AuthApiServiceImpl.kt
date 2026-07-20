package com.nullo.voidapp.core.network.auth.service

import com.nullo.voidapp.core.network.auth.dto.ExchangeCodeRequest
import com.nullo.voidapp.core.network.auth.dto.ExchangeCodeResponse
import com.nullo.voidapp.core.network.auth.exception.CodeExchangeException
import com.nullo.voidapp.core.network.auth.exception.InternalServerException
import com.nullo.voidapp.core.network.auth.exception.InvalidApiKeyException
import com.nullo.voidapp.core.network.auth.exception.UnexpectedStatusException
import com.nullo.voidapp.core.network.auth.exception.ValidationNetworkException
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
        if (code == null) throw CodeExchangeException()

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
            throw CodeExchangeException(e)
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
            throw ValidationNetworkException(e)
        }

        when (response.status) {
            HttpStatusCode.OK -> return
            HttpStatusCode.Unauthorized -> throw InvalidApiKeyException()
            HttpStatusCode.InternalServerError -> throw InternalServerException()
            else -> throw UnexpectedStatusException()
        }
    }

    companion object {

        const val CODE_EXCHANGE_ENDPOINT = "auth/keys"
        const val CHECK_API_KEY_ENDPOINT = "key"
    }
}
