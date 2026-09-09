package com.borasarang.macjupjup.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.borasarang.macjupjup.data.db.entity.AppSourceMapping

@Dao
interface AppSourceMappingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(mappings: List<AppSourceMapping>)

    @Query("SELECT * FROM app_sources WHERE appId = :appId")
    suspend fun getByApp(appId: String): List<AppSourceMapping>
}
