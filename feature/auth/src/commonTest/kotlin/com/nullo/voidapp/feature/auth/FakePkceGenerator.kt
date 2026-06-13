package com.nullo.voidapp.feature.auth

import com.nullo.voidapp.feature.auth.domain.entity.PkceParams
import com.nullo.voidapp.feature.auth.domain.util.PkceGenerator

internal class FakePkceGenerator(
    private val codeVerifier: String = "test_code_verifier",
    private val codeChallenge: String = "test_code_challenge",
    private val codeChallengeMethod: String = "S256",
) : PkceGenerator {

    override fun generate(): PkceParams = PkceParams(
        codeVerifier = codeVerifier,
        codeChallenge = codeChallenge,
        codeChallengeMethod = codeChallengeMethod,
    )
}
