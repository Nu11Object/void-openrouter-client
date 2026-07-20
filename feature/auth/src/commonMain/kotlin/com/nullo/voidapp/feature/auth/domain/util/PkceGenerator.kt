package com.nullo.voidapp.feature.auth.domain.util

import com.nullo.voidapp.feature.auth.domain.entity.PkceParams

/** Generates secure PKCE parameters for OAuth authorization. */
internal interface PkceGenerator {

    /** Generates a new [PkceParams] using a secure random verifier and SHA-256 hashing. */
    fun generate(): PkceParams
}
