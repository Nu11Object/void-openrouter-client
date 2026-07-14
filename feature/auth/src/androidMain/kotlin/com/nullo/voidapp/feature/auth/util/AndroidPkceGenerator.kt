package com.nullo.voidapp.feature.auth.util

import com.nullo.voidapp.feature.auth.util.entity.PkceParams
import com.nullo.voidapp.feature.auth.util.util.PkceGenerator
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

internal object AndroidPkceGenerator : PkceGenerator {

    private const val VERIFIER_BYTE_LENGTH = 64

    override fun generate(): PkceParams {
        val verifierBytes = ByteArray(VERIFIER_BYTE_LENGTH)
            .also { SecureRandom().nextBytes(it) }

        val codeVerifier = verifierBytes.encodeBase64Url()
        val codeChallenge = MessageDigest
            .getInstance("SHA-256")
            .digest(codeVerifier.toByteArray(Charsets.US_ASCII))
            .encodeBase64Url()

        return PkceParams(codeVerifier = codeVerifier, codeChallenge = codeChallenge)
    }

    private fun ByteArray.encodeBase64Url(): String = Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(this)
}
