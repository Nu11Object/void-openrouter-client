package com.nullo.voidapp.feature.auth.domain.util

import com.nullo.voidapp.feature.auth.domain.entity.PkceParams
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Foundation.NSData
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.dataWithBytes
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault

@OptIn(ExperimentalForeignApi::class)
internal object IosPkceGenerator : PkceGenerator {
    private const val VERIFIER_BYTE_LENGTH = 64

    override fun generate(): PkceParams {
        val verifierBytes = secureRandom(VERIFIER_BYTE_LENGTH)
        val codeVerifier = verifierBytes.encodeBase64Url()
        val codeChallenge = sha256(codeVerifier.toByteArray(Charsets.UTF_8)).encodeBase64Url()
        return PkceParams(codeVerifier = codeVerifier, codeChallenge = codeChallenge)
    }

    private fun secureRandom(length: Int): ByteArray {
        val buffer = ByteArray(length)
        buffer.usePinned { pinned ->
            val status = SecRandomCopyBytes(
                rnd = kSecRandomDefault,
                count = length.toULong(),
                bytes = pinned.addressOf(0),
            )
            check(status == errSecSuccess) { "SecRandomCopyBytes failed: $status" }
        }
        return buffer
    }

    private fun sha256(input: ByteArray): ByteArray {
        val digest = ByteArray(CC_SHA256_DIGEST_LENGTH)
        input.usePinned { pinnedIn ->
            digest.usePinned { pinnedOut ->
                CC_SHA256(
                    data = pinnedIn.addressOf(0),
                    len = input.size.convert(),
                    md = pinnedOut.addressOf(0).reinterpret(),
                )
            }
        }
        return digest
    }

    private fun ByteArray.encodeBase64Url(): String {
        val nsData = usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), size.toULong())
        }

        return nsData.base64EncodedStringWithOptions(0u)
            .replace('+', '-')
            .replace('/', '_')
            .trimEnd('=')
    }
}
