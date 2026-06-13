package com.nullo.voidapp.feature.auth.presentation

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.browser.auth.AuthTabIntent
import com.nullo.voidapp.feature.auth.data.OAuthCallbackDispatcher

/**
 * Binds the platform-level ActivityResultLauncher to the internal OAuth flow.
 * Must be called exactly once during onCreate() before the host Activity starts.
 */
interface AuthTabResultRegistrar {

    fun register(activity: ComponentActivity)
}

internal class DefaultAuthTabResultRegistrar(
    private val launcherHolder: AuthTabLauncherHolder,
    private val dispatcher: OAuthCallbackDispatcher,
) : AuthTabResultRegistrar {

    override fun register(activity: ComponentActivity) {
        launcherHolder.launcher = AuthTabIntent.registerActivityResultLauncher(activity) { result ->
            if (result.resultCode == AuthTabIntent.RESULT_OK) {
                result.resultUri?.let { uri -> dispatcher.onCallbackUrl(uri.toString()) }
            }
        }
    }
}

internal class AuthTabLauncherHolder {

    @Volatile
    var launcher: ActivityResultLauncher<Intent>? = null
}
