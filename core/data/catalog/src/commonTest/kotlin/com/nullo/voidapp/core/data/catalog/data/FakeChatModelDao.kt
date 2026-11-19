package com.nullo.voidapp.core.data.catalog.data

import com.nullo.voidapp.core.database.dao.ChatModelDao
import com.nullo.voidapp.core.database.entity.ChatModelEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

internal class FakeChatModelDao : ChatModelDao {

    private val models = MutableStateFlow<List<ChatModelEntity>>(emptyList())

    override suspend fun insertAll(items: List<ChatModelEntity>) {
        models.update { current ->
            val updated = current.toMutableList()
            items.forEach { item ->
                val index = updated.indexOfFirst { it.id == item.id }
                if (index == -1) {
                    updated.add(item)
                } else {
                    updated[index] = item
                }
            }
            updated
        }
    }

    override fun observeAll(): Flow<List<ChatModelEntity>> = models

    override fun observeById(modelId: String): Flow<ChatModelEntity?> =
        models.map { items -> items.firstOrNull { it.id == modelId } }

    override fun observePinned(): Flow<List<ChatModelEntity>> =
        models.map { items -> items.filter { it.pinned } }

    override suspend fun count(): Int = models.value.size

    override suspend fun setPinned(modelId: String, pinned: Boolean) {
        models.update { current ->
            current.map { item ->
                if (item.id == modelId) item.copy(pinned = pinned) else item
            }
        }
    }

    override suspend fun getPinnedIds(): List<String> =
        models.value.filter { it.pinned }.map { it.id }

    override suspend fun deleteAll() {
        models.update { emptyList() }
    }
}
