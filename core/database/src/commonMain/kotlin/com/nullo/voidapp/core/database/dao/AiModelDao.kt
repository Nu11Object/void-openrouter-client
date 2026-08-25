package com.nullo.voidapp.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.nullo.voidapp.core.database.entity.AiModelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiModelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AiModelEntity>)

    @Query("SELECT * FROM ai_models")
    fun observeAll(): Flow<List<AiModelEntity>>

    @Query("SELECT * FROM ai_models WHERE pinned = 1")
    fun observePinned(): Flow<List<AiModelEntity>>

    @Query("SELECT COUNT(*) FROM ai_models")
    suspend fun count(): Int

    @Query("UPDATE ai_models SET pinned = :pinned WHERE id = :modelId")
    suspend fun setPinned(modelId: String, pinned: Boolean)

    @Transaction
    suspend fun replaceAll(items: List<AiModelEntity>) {
        val pinnedIds = getPinnedIds().toHashSet()
        deleteAll()
        insertAll(
            items.map { item ->
                if (item.id in pinnedIds) item.copy(pinned = true) else item
            }
        )
    }

    @Query("SELECT id FROM ai_models WHERE pinned = 1")
    suspend fun getPinnedIds(): List<String>

    @Query("DELETE FROM ai_models")
    suspend fun deleteAll()
}