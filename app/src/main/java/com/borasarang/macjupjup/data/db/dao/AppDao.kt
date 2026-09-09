package com.borasarang.macjupjup.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.borasarang.macjupjup.data.db.entity.App
import com.borasarang.macjupjup.data.db.entity.AppSourceMapping

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(apps: List<App>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(app: App): Long

    @Query("SELECT * FROM apps WHERE id = :id")
    suspend fun getById(id: String): App?

    @Query("SELECT COUNT(*) FROM apps")
    suspend fun count(): Int

    @Query(
        """SELECT * FROM apps
        WHERE (:license IS NULL OR license = :license)
          AND (:category IS NULL OR category = :category)
          AND (:tag IS NULL OR tags LIKE '%' || :tag || '%')
          AND (:q IS NULL OR name LIKE '%' || :q || '%' OR developer LIKE '%' || :q || '%')
        ORDER BY
          CASE WHEN :sort = 'stars' THEN stars END DESC,
          CASE WHEN :sort = 'rating' THEN averageRating END DESC,
          CASE WHEN :sort = 'updated' THEN lastUpdatedAt END DESC,
          lastUpdatedAt DESC
        LIMIT :limit OFFSET :offset"""
    )
    suspend fun listFiltered(
        license: String?,
        category: String?,
        tag: String?,
        q: String?,
        sort: String,
        limit: Int,
        offset: Int,
    ): List<App>

    @Query(
        """SELECT COUNT(*) FROM apps
        WHERE (:license IS NULL OR license = :license)
          AND (:category IS NULL OR category = :category)
          AND (:tag IS NULL OR tags LIKE '%' || :tag || '%')
          AND (:q IS NULL OR name LIKE '%' || :q || '%' OR developer LIKE '%' || :q || '%')"""
    )
    suspend fun countFiltered(
        license: String?,
        category: String?,
        tag: String?,
        q: String?,
    ): Int

    @Query("SELECT * FROM apps WHERE trackId = :trackId LIMIT 1")
    suspend fun getByTrackId(trackId: Long): App?

    @Query("SELECT * FROM apps WHERE repoFullName IS NOT NULL ORDER BY lastUpdatedAt ASC LIMIT :limit")
    suspend fun getReposForReleaseCheck(limit: Int): List<App>

    @Query("SELECT * FROM apps WHERE trackId IS NOT NULL ORDER BY lastUpdatedAt ASC LIMIT :limit")
    suspend fun getAppsWithTrackId(limit: Int): List<App>

    @Query("SELECT * FROM apps WHERE trackId IS NULL AND repoFullName IS NULL ORDER BY lastUpdatedAt ASC LIMIT :limit")
    suspend fun getAppsWithoutIds(limit: Int): List<App>

    @Query("SELECT * FROM apps WHERE sourceId = 'setapp_seed' AND iconUrl IS NULL ORDER BY lastUpdatedAt ASC LIMIT :limit")
    suspend fun getSetappWithoutIcon(limit: Int): List<App>

    /** 콤마 구분자 시절에 깨진 Setapp CDN 스크린샷 정리 (v1.1 1회성 복구) */
    @Query("UPDATE apps SET screenshotUrls = NULL WHERE screenshotUrls LIKE '%cdn-cgi%'")
    suspend fun clearBrokenCdnScreenshots(): Int

    @Query(
        """SELECT * FROM apps
        WHERE (descriptionKo IS NULL AND descriptionSnippet IS NOT NULL)
           OR (releaseNotesKo IS NULL AND releaseNotes IS NOT NULL)
        ORDER BY lastUpdatedAt DESC LIMIT :limit"""
    )
    suspend fun getUntranslated(limit: Int): List<App>

    @Query("UPDATE apps SET descriptionKo = :descKo, releaseNotesKo = :notesKo WHERE id = :id")
    suspend fun updateKo(id: String, descKo: String?, notesKo: String?)

    // ---------- 트렌드 집계 (T-041) ----------

    @Query("SELECT category AS name, COUNT(*) AS cnt FROM apps GROUP BY category ORDER BY cnt DESC")
    suspend fun countByCategory(): List<NameCount>

    @Query("SELECT license AS name, COUNT(*) AS cnt FROM apps GROUP BY license")
    suspend fun countByLicense(): List<NameCount>

    @Query("SELECT COUNT(*) FROM apps WHERE firstSeenAt >= :since")
    suspend fun countNewSince(since: Long): Int

    @Query("SELECT COUNT(*) FROM apps WHERE lastUpdatedAt >= :since AND isNew = 0")
    suspend fun countUpdatedSince(since: Long): Int

    @Query("SELECT COUNT(*) FROM apps WHERE tags LIKE '%AI-Agent%'")
    suspend fun countAiTag(): Int

    @Query("SELECT COUNT(*) FROM apps WHERE tags LIKE '%MenuBar%'")
    suspend fun countMenuBarTag(): Int

    @Query("SELECT COUNT(*) FROM version_history WHERE detectedAt >= :since")
    suspend fun countVersionBumpsSince(since: Long): Int

    @Query("SELECT * FROM apps WHERE repoFullName = :repo LIMIT 1")
    suspend fun getByRepo(repo: String): App?

    @Query("DELETE FROM apps WHERE lastUpdatedAt < :before")
    suspend fun deleteOlderThan(before: Long): Int

    @Transaction
    @Query("SELECT * FROM apps WHERE id = :id")
    suspend fun getWithSources(id: String): AppWithSources?
}

/** 집계 결과 공용 */
data class NameCount(
    val name: String,
    val cnt: Int,
)

/** 앱 + 출처 묶음 (상세 API 응답용) */
data class AppWithSources(
    @androidx.room.Embedded val app: App,
    @androidx.room.Relation(parentColumn = "id", entityColumn = "appId")
    val sources: List<AppSourceMapping>,
)
