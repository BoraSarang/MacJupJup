package com.borasarang.macjupjup.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.borasarang.macjupjup.data.db.entity.CrawlLog

@Dao
interface CrawlLogDao {
    @Insert
    suspend fun insert(log: CrawlLog): Long

    @Query("UPDATE crawl_logs SET finishedAt = :finishedAt, status = :status, plansFound = :found, plansNew = :newCount, plansUpdated = :updated, errorMessage = :error WHERE id = :id")
    suspend fun finish(id: Long, finishedAt: Long, status: String, found: Int, newCount: Int, updated: Int, error: String?)

    @Query("SELECT * FROM crawl_logs ORDER BY startedAt DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<CrawlLog>

    @Query("SELECT * FROM crawl_logs WHERE sourceId = :sourceId ORDER BY startedAt DESC LIMIT 1")
    suspend fun latestBySource(sourceId: String): CrawlLog?

    @Query("SELECT * FROM crawl_logs WHERE sourceId = :sourceId ORDER BY startedAt DESC LIMIT :limit")
    suspend fun recentBySource(sourceId: String, limit: Int): List<CrawlLog>

    @Query("DELETE FROM crawl_logs WHERE startedAt < :before")
    suspend fun deleteOlderThan(before: Long): Int
}
