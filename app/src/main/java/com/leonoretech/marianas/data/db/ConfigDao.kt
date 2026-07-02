package com.leonoretech.marianas.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigDao {

    @Query("SELECT * FROM config WHERE id = 0 LIMIT 1")
    fun observe(): Flow<ConfigEntity?>

    @Query("SELECT * FROM config WHERE id = 0 LIMIT 1")
    suspend fun get(): ConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: ConfigEntity)

    @Query("DELETE FROM config")
    suspend fun clear()
}
