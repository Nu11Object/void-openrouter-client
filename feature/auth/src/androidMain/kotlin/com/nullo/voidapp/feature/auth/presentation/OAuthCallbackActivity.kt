package com.nullo.voidapp.feature.auth.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.nullo.voidapp.feature.auth.data.OAuthCallbackDispatcher
import org.koin.android.ext.android.inject
import org.koin.java.KoinJavaComponent.inject

/**
 * A fallback [Activity] used to capture OAuth redirect deep links.
 *
 * This activity handles the callback URL when the host device does not support modern
 * OAuth flows (like AuthTab). It intercepts the intent via the manifest's intent-filter,
 * forwards the callback URL to the [OAuthCallbackDispatcher], and immediately finishes.
 */
internal class OAuthCallbackActivity : Activity() {

    private val dispatcher: OAuthCallbackDispatcher by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
        finish()
    }

    private fun handleIntent(intent: Intent?) {
        val callbackUrl = intent?.data?.toString() ?: return
        dispatcher.onCallbackUrl(callbackUrl)
    }
}
