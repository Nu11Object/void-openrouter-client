package com.nullo.voidapp.core.security

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlock
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class IosApiKeyStorage : ApiKeyStorage {

    private val hasKeyState = MutableStateFlow<Boolean?>(null)

    override fun hasApiKey(): Flow<Boolean> = flow {
        if (hasKeyState.value == null) {
            val exists = runCatching { checkHasKeyNative() }.getOrDefault(false)
            hasKeyState.value = exists
        }
        emitAll(hasKeyState.filterNotNull())
    }.distinctUntilChanged()

    private suspend fun checkHasKeyNative(): Boolean = withContext(Dispatchers.IO) {
        val query = buildBaseQuery()
        val status = SecItemCopyMatching(query, null)
        CFRelease(query)
        status == errSecSuccess
    }

    override suspend fun saveApiKey(newKey: String): Unit = withContext(Dispatchers.IO) {
        deleteApiKeyNative()

        val data = (newKey as NSString).dataUsingEncoding(NSUTF8StringEncoding)
            ?: throw SecureStorageException("iOS Keychain: Failed to encode API key to UTF-8")

        val cfData = CFBridgingRetain(data)
        val query = buildBaseQuery()
        CFDictionarySetValue(query, kSecValueData, cfData)
        CFDictionarySetValue(query, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlock)

        val status = SecItemAdd(query, null)
        CFRelease(cfData)
        CFRelease(query)

        if (status != errSecSuccess) {
            throw SecureStorageException("iOS Keychain SecItemAdd failed with status: $status")
        }

        hasKeyState.value = true
    }

    override suspend fun getApiKey(): String? = withContext(Dispatchers.IO) {
        val query = buildBaseQuery()
        CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)

        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, result.ptr)
            CFRelease(query)

            if (status == errSecItemNotFound) return@withContext null

            if (status != errSecSuccess) {
                throw SecureStorageException(
                    "iOS Keychain SecItemCopyMatching failed with status: $status"
                )
            }

            val data = CFBridgingRelease(result.value) as? NSData ?: return@withContext null
            NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
        }
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        deleteApiKeyNative()
        hasKeyState.value = false
    }

    private fun deleteApiKeyNative() {
        val query = buildBaseQuery()
        val status = SecItemDelete(query)
        CFRelease(query)

        if (status != errSecSuccess && status != errSecItemNotFound) {
            throw SecureStorageException(
                "iOS Keychain SecItemDelete failed with status: $status"
            )
        }
    }

    private fun buildBaseQuery(): CFMutableDictionaryRef {
        val dict = CFDictionaryCreateMutable(
            null, 0,
            kCFTypeDictionaryKeyCallBacks.ptr, kCFTypeDictionaryValueCallBacks.ptr
        )!!
        CFDictionarySetValue(dict, kSecClass, kSecClassGenericPassword)

        val serviceRef = CFBridgingRetain(SERVICE as NSString)
        val accountRef = CFBridgingRetain(ACCOUNT as NSString)
        CFDictionarySetValue(dict, kSecAttrService, serviceRef)
        CFDictionarySetValue(dict, kSecAttrAccount, accountRef)
        CFRelease(serviceRef)
        CFRelease(accountRef)

        return dict
    }

    companion object {
        private const val SERVICE = "com.nullo.voidapp"
        private const val ACCOUNT = "api_key"
    }
}
