package com.nullo.voidapp

import androidx.compose.runtime.Composable
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import io.github.vinceglb.filekit.coil.addPlatformFileSupport

/**
 * Configures the app-wide Coil [ImageLoader].
 * Must be called before any [AsyncImage][coil3.compose.AsyncImage] is composed.
 */
@Composable
internal fun SetupImageLoader() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { addPlatformFileSupport() }
            .build()
    }
}
