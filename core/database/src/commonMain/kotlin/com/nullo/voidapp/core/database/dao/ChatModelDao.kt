package com.nullo.voidapp.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.nullo.voidapp.core.database.entity.ChatModelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatModelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ChatModelEntity>)

    @Query("SELECT * FROM chat_models")
    fun observeAll(): Flow<List<ChatModelEntity>>

    @Query("SELECT * FROM chat_models WHERE id = :modelId LIMIT 1")
    fun observeById(modelId: String): Flow<ChatModelEntity?>

    @Query("SELECT * FROM chat_models WHERE pinned = 1")
    fun observePinned(): Flow<List<ChatModelEntity>>

    @Query("SELECT COUNT(*) FROM chat_models")
    suspend fun count(): Int

    @Query("UPDATE chat_models SET pinned = :pinned WHERE id = :modelId")
    suspend fun setPinned(modelId: String, pinned: Boolean)

    @Transaction
    suspend fun replaceAll(items: List<ChatModelEntity>) {
        val pinnedIds = getPinnedIds().toHashSet()
        deleteAll()
        insertAll(
            items.map { item ->
                if (item.id in pinnedIds) item.copy(pinned = true) else item
            }
        )
    }

    @Query("SELECT id FROM chat_models WHERE pinned = 1")
    suspend fun getPinnedIds(): List<String>

    @Query("DELETE FROM chat_models")
    suspend fun deleteAll()
}
