package com.nullo.voidapp.feature.auth

import com.nullo.voidapp.feature.auth.domain.entity.PkceParams
import com.nullo.voidapp.feature.auth.domain.util.PkceGenerator

internal class FakePkceGenerator : PkceGenerator {

    var params: PkceParams = PkceParams("test-verifier", "test-challenge")

    var calls = 0
        private set

    override fun generate(): PkceParams {
        calls++
        return params
    }
}
