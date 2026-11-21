package com.nullo.voidapp.core.auth

import kotlinx.serialization.json.Json

internal val authTestJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
}
