package com.borasarang.macjupjup.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.borasarang.macjupjup.data.db.entity.VersionHistory

@Dao
interface VersionHistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: VersionHistory): Long

    @Query("SELECT * FROM version_history WHERE appId = :appId ORDER BY detectedAt DESC")
    suspend fun getByApp(appId: String): List<VersionHistory>

    @Query("SELECT * FROM version_history WHERE appId = :appId ORDER BY detectedAt DESC LIMIT 1")
    suspend fun getLatest(appId: String): VersionHistory?

    @Query("DELETE FROM version_history WHERE detectedAt < :before")
    suspend fun deleteOlderThan(before: Long): Int
}
