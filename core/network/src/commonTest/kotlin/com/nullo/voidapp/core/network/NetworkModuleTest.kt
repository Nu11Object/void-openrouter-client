package com.nullo.voidapp.core.network

import com.nullo.voidapp.core.security.ApiKeyStorage
import com.nullo.voidapp.core.testing.FakeApiKeyStorage
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.dsl.koinApplication
import org.koin.dsl.module

@Serializable
private class ModulePayload(
    val value: String = "default",
    val optional: String? = null,
)

private class NetworkModuleEnv {
    private val application = koinApplication {
        modules(
            module {
                single<ApiKeyStorage> { FakeApiKeyStorage() }
            },
            networkModule
        )
    }
    val koin = application.koin
    val client = koin.get<HttpClient>()

    fun close() {
        client.close()
        application.close()
    }
}

val NetworkModuleTest by testSuite {
    testFixture { NetworkModuleEnv() } closeWith { close() } asParameterForEach {
        testSuite("json") {
            test("the Json ignores unknown keys when decoding") { env ->
                val payload = env.koin.get<Json>()
                    .decodeFromString<ModulePayload>("""{"value":"decoded","unknown":true}""")

                payload.value shouldBe "decoded"
            }

            test("the Json omits null properties when encoding") { env ->
                val encoded = env.koin.get<Json>()
                    .encodeToString(ModulePayload(value = "encoded", optional = null))

                encoded shouldBe """{"value":"encoded"}"""
            }

            test("the Json encodes properties with default values") { env ->
                val encoded = env.koin.get<Json>().encodeToString(ModulePayload())

                encoded shouldBe """{"value":"default"}"""
            }
        }
    }
}
