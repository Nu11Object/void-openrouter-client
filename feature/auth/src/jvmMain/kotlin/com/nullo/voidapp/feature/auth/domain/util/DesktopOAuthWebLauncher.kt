package com.nullo.voidapp.feature.auth.domain.util

import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.feature.auth.data.network.AuthConfig
import com.nullo.voidapp.feature.auth.exception.OAuthCancelledException
import com.nullo.voidapp.feature.auth.exception.OAuthException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import voidapp.feature.auth.generated.resources.Res
import voidapp.feature.auth.generated.resources.exception_browser_cannot_open
import voidapp.feature.auth.generated.resources.exception_browser_unsupported
import voidapp.feature.auth.generated.resources.exception_empty_request
import voidapp.feature.auth.generated.resources.oauth_success_message
import voidapp.feature.auth.generated.resources.oauth_success_title
import java.awt.Desktop
import java.net.ServerSocket
import java.net.SocketTimeoutException
import java.net.URI

internal class DesktopOAuthWebLauncher : OAuthWebLauncher {

    @Volatile
    private var currentPort: Int = 0

    override fun getCallbackUrl(): String {
        val port = findFreePort()
        currentPort = port
        return "http://localhost:$port/callback"
    }

    override suspend fun launch(authUrl: String): String = withContext(Dispatchers.IO) {
        val port = currentPort

        ServerSocket(port)
            .apply { soTimeout = AuthConfig.OAUTH_TIMEOUT_MS.toInt() }
            .use { server ->
                openSystemBrowser(authUrl)

                val socket = try {
                    server.accept()
                } catch (e: SocketTimeoutException) {
                    throw OAuthCancelledException(e)
                }

                socket.use { conn ->
                    val requestLine = conn.getInputStream()
                        .bufferedReader()
                        .readLine()
                        ?: throw OAuthException(UiText(Res.string.exception_empty_request))

                    val path = requestLine
                        .removePrefix("GET ")
                        .substringBefore(" HTTP")

                    val title = getString(Res.string.oauth_success_title)
                    val message = getString(Res.string.oauth_success_message)
                    val html = buildSuccessHtml(title, message)

                    val httpResponse = buildString {
                        append("HTTP/1.1 200 OK\r\n")
                        append("Content-Type: text/html; charset=utf-8\r\n")
                        append("Content-Length: ${html.encodeToByteArray().size}\r\n")
                        append("Connection: close\r\n")
                        append("\r\n")
                        append(html)
                    }
                    conn.getOutputStream().also {
                        it.write(httpResponse.toByteArray())
                        it.flush()
                    }

                    "http://localhost:$port$path"
                }
            }
    }

    private fun openSystemBrowser(url: String) {
        val isSupported = Desktop.isDesktopSupported() &&
                Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)

        if (!isSupported) {
            throw OAuthException(UiText(Res.string.exception_browser_unsupported))
        }

        runCatching { Desktop.getDesktop().browse(URI(url)) }
            .onFailure { cause ->
                throw OAuthException(
                    uiText = UiText(
                        Res.string.exception_browser_cannot_open,
                        cause.message.toString()
                    ),
                    cause = cause
                )
            }
    }

    private fun findFreePort(): Int = ServerSocket(0).use { it.localPort }

    private fun buildSuccessHtml(title: String, message: String) = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <title>$title</title>
          <style>
            body { font-family: system-ui, sans-serif; background: #03080A;
                    display: flex; align-items: center; justify-content: center;
                    min-height: 100vh; margin: 0; }
            .card { background: #0C1113; padding: 32px 48px; border-radius: 24px; 
                    text-align: center; }
            h1 { color: #C9FF00; margin: 0 0 12px; }
            p  { color: #FCFCFE; margin: 0; }
          </style>
        </head>
        <body>
          <div class="card">
            <h1>$title</h1>
            <p>$message</p>
          </div>
        </body>
        </html>
    """.trimIndent()
}
