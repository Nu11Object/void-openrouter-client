package com.nullo.voidapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun App() {
    CompositionLocalProvider(LocalPlatform provides getPlatform()) {
        MaterialTheme {

        }
    }
}