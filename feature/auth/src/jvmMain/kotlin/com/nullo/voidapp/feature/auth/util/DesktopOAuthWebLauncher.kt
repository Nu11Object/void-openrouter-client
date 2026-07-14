package com.nullo.voidapp.feature.auth.util

import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.feature.auth.data.network.AuthConfig
import com.nullo.voidapp.feature.auth.util.entity.OAuthCancelledException
import com.nullo.voidapp.feature.auth.util.entity.OAuthException
import com.nullo.voidapp.feature.auth.util.util.OAuthWebLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import voidapp.feature.auth.generated.resources.Res
import voidapp.feature.auth.generated.resources.exception_browser_cannot_open
import voidapp.feature.auth.generated.resources.exception_browser_unsupported
import voidapp.feature.auth.generated.resources.exception_empty_request
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
                } catch (_: SocketTimeoutException) {
                    throw OAuthCancelledException()
                }

                socket.use { conn ->
                    val requestLine = conn.getInputStream()
                        .bufferedReader()
                        .readLine()
                        ?: throw OAuthException(UiText(Res.string.exception_empty_request))

                    val path = requestLine
                        .removePrefix("GET ")
                        .substringBefore(" HTTP")

                    val html = buildSuccessHtml()
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
            .onFailure {
                throw OAuthException(
                    uiText = UiText(
                        Res.string.exception_browser_cannot_open,
                        it.message.toString()
                    ),
                    cause = it
                )
            }
    }

    private fun findFreePort(): Int = ServerSocket(0).use { it.localPort }

    private fun buildSuccessHtml() = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <title>Authorized</title>
          <style>
            body { font-family: system-ui, sans-serif; background: #13111A;
                    display: flex; align-items: center; justify-content: center;
                    min-height: 100vh; margin: 0; }
            .card { background: #1E1A2B; padding: 32px 48px; border-radius: 24px; 
                    text-align: center; }
            h1 { color: #A855F7; margin: 0 0 12px; }
            p  { color: #CAC4D0; margin: 0; }
          </style>
        </head>
        <body>
          <div class="card">
            <h1>Authorized!</h1>
            <p>You can close this tab and return to the app.</p>
          </div>
        </body>
        </html>
    """.trimIndent()
}
