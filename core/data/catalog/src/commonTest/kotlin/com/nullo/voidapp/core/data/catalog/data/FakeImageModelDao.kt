package com.nullo.voidapp.core.data.catalog.data

import com.nullo.voidapp.core.database.dao.ImageModelDao
import com.nullo.voidapp.core.database.entity.ImageModelEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

internal class FakeImageModelDao : ImageModelDao {

    private val models = MutableStateFlow<List<ImageModelEntity>>(emptyList())

    override suspend fun insertAll(items: List<ImageModelEntity>) {
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

    override fun observeAll(): Flow<List<ImageModelEntity>> = models

    override fun observePinned(): Flow<List<ImageModelEntity>> =
        models.map { items -> items.filter { it.pinned } }

    override fun observeById(modelId: String): Flow<ImageModelEntity?> =
        models.map { items -> items.firstOrNull { it.id == modelId } }

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
