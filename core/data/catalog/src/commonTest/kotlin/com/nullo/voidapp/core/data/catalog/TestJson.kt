package com.nullo.voidapp.core.data.catalog

import kotlinx.serialization.json.Json

internal val catalogTestJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
}
