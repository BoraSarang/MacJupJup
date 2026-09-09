package com.borasarang.macjupjup.data.repository

import com.borasarang.macjupjup.data.db.MacDatabase
import com.borasarang.macjupjup.data.db.entity.App
import com.borasarang.macjupjup.data.db.entity.AppSourceMapping
import com.borasarang.macjupjup.data.db.entity.VersionHistory
import com.borasarang.macjupjup.util.Constants
import com.borasarang.macjupjup.util.DebugLogger
import com.borasarang.macjupjup.util.TimeUtils

/** 앱 저장·조회·병합·정리 */
class AppRepository(private val db: MacDatabase) {

    suspend fun saveApps(
        apps: List<App>,
        mappings: List<AppSourceMapping>,
    ): SaveResult {
        if (apps.isEmpty()) return SaveResult(0, 0, 0)
        val dao = db.appDao()
        var created = 0
        var updated = 0
        val createdIds = mutableListOf<String>()
        val merged = apps.map { draft ->
            val existing = dao.getById(draft.id)
            if (existing == null) {
                created++
                createdIds += draft.id
                draft
            } else {
                updated++
                mergeApps(existing, draft)
            }
        }
        dao.upsertAll(merged)
        if (mappings.isNotEmpty()) db.appSourceMappingDao().upsertAll(mappings)
        // 버전 히스토리 기록 (신규 버전만, 병합 후 기준)
        val now = System.currentTimeMillis()
        for (app in merged) {
            val v = app.version ?: continue
            val latest = db.versionHistoryDao().getLatest(app.id)
            if (latest == null || latest.version != v) {
                if (latest != null) {
                    DebugLogger.i("버전추적", "버전 bump 감지: ${app.name} ${latest.version} → $v")
                }
                db.versionHistoryDao().insert(
                    VersionHistory(
                        appId = app.id,
                        version = v,
                        detectedAt = now,
                        notesSummary = app.releaseNotesSummary,
                        source = app.sourceId,
                    )
                )
            }
        }
        return SaveResult(apps.size, created, updated, createdIds)
    }

    suspend fun list(filter: AppFilter): PagedApps {
        val pageSize = filter.pageSize.coerceIn(1, Constants.API_MAX_PAGE_SIZE)
        val page = filter.page.coerceAtLeast(1)
        val offset = (page - 1) * pageSize
        val q = filter.q?.ifBlank { null }
        val apps = db.appDao().listFiltered(
            license = filter.license,
            category = filter.category,
            tag = filter.tag,
            q = q,
            sort = filter.sort,
            limit = pageSize,
            offset = offset,
        )
        val total = db.appDao().countFiltered(filter.license, filter.category, filter.tag, q)
        val items = apps.map { a ->
            val mapping = db.appSourceMappingDao().getByApp(a.id).firstOrNull()
            AppListItem(
                app = a,
                sourceName = mapping?.sourceName,
                sourceUrl = mapping?.sourceUrl,
            )
        }
        return PagedApps(items, total, page, pageSize)
    }

    suspend fun detail(id: String): AppWithSourceList? {
        val withSources = db.appDao().getWithSources(id) ?: return null
        val versions = db.versionHistoryDao().getByApp(id)
        return withSources.toModel(versions)
    }

    suspend fun overview(): AppStats {
        val total = db.appDao().count()
        val active = db.crawlSourceDao().getEnabled().size
        val lastRun = db.crawlSourceDao().getAll().mapNotNull { it.lastRunAt }.maxOrNull()
        return AppStats(total, active, lastRun)
    }

    /** 트렌드 대시보드 집계 (T-041) */
    suspend fun trends(): TrendStats {
        val dao = db.appDao()
        val weekAgo = System.currentTimeMillis() - 7 * TimeUtils.MILLIS_PER_DAY
        return TrendStats(
            byCategory = dao.countByCategory().associate { it.name to it.cnt },
            byLicense = dao.countByLicense().associate { it.name to it.cnt },
            newLast7d = dao.countNewSince(weekAgo),
            updatedLast7d = dao.countUpdatedSince(weekAgo),
            versionBumpsLast7d = dao.countVersionBumpsSince(weekAgo),
            aiTagCount = dao.countAiTag(),
            menuBarTagCount = dao.countMenuBarTag(),
        )
    }

    /**
     * 재수집 병합: draft null → 기존값 유지 (보강분 보호).
     * 설명·노트는 버전 변경 시 draft, 아니면 긴 쪽. 라이선스는 OSS>PAID>FREE 우선.
     * firstSeenAt·isNew·수동 오버라이드는 기존 유지.
     */
    internal fun mergeApps(existing: App, draft: App): App {
        val versionChanged = draft.version != null && existing.version != null &&
            draft.version != existing.version
        return draft.copy(
            firstSeenAt = existing.firstSeenAt,
            isNew = existing.isNew,
            licenseOverride = existing.licenseOverride,
            license = maxLicense(existing.license, draft.license),
            descriptionSnippet = longer(existing.descriptionSnippet, draft.descriptionSnippet),
            releaseNotes = if (versionChanged) {
                draft.releaseNotes ?: existing.releaseNotes
            } else {
                longer(existing.releaseNotes, draft.releaseNotes)
            },
            releaseNotesSummary = if (versionChanged) {
                draft.releaseNotesSummary ?: existing.releaseNotesSummary
            } else {
                draft.releaseNotesSummary ?: existing.releaseNotesSummary
            },
            iconUrl = draft.iconUrl ?: existing.iconUrl,
            descriptionKo = draft.descriptionKo ?: existing.descriptionKo,
            releaseNotesKo = if (versionChanged) draft.releaseNotesKo else {
                draft.releaseNotesKo ?: existing.releaseNotesKo
            },
            screenshotUrls = draft.screenshotUrls ?: existing.screenshotUrls,
            sellerName = draft.sellerName ?: existing.sellerName,
            fileSize = draft.fileSize ?: existing.fileSize,
            minOs = draft.minOs ?: existing.minOs,
            contentRating = draft.contentRating ?: existing.contentRating,
            stars = draft.stars ?: existing.stars,
            averageRating = draft.averageRating ?: existing.averageRating,
            ratingCount = draft.ratingCount ?: existing.ratingCount,
            primaryLanguage = draft.primaryLanguage ?: existing.primaryLanguage,
            topics = draft.topics ?: existing.topics,
            forks = draft.forks ?: existing.forks,
            issues = draft.issues ?: existing.issues,
            licenseName = draft.licenseName ?: existing.licenseName,
            homepageUrl = draft.homepageUrl ?: existing.homepageUrl,
            repoFullName = draft.repoFullName ?: existing.repoFullName,
            trackId = draft.trackId ?: existing.trackId,
            price = if (draft.price > 0) draft.price else existing.price,
            // 카테고리는 최초 분류 유지 (필터 안정성). 태그는 합집합
            category = existing.category,
            tags = unionTags(existing.tags, draft.tags),
            version = draft.version ?: existing.version,
            prevVersion = if (versionChanged) existing.version else {
                draft.prevVersion ?: existing.prevVersion
            },
            releaseDate = draft.releaseDate ?: existing.releaseDate,
            sourceId = existing.sourceId,
        )
    }

    private fun longer(a: String?, b: String?): String? {        if (a.isNullOrBlank()) return b
        if (b.isNullOrBlank()) return a
        return if (b.length > a.length) b else a
    }

    private fun maxLicense(a: String, b: String): String {        val rank = mapOf(
            com.borasarang.macjupjup.util.Constants.LICENSE_OSS to 3,
            com.borasarang.macjupjup.util.Constants.LICENSE_PAID to 2,
            com.borasarang.macjupjup.util.Constants.LICENSE_FREE to 1,
        )
        return if ((rank[b] ?: 0) >= (rank[a] ?: 0)) b else a
    }

    private fun unionTags(a: String?, b: String?): String? {
        val set = LinkedHashSet<String>()
        a?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.let { set.addAll(it) }
        b?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.let { set.addAll(it) }
        return set.joinToString(",").ifBlank { null }
    }

    /** 보관기간 초과 데이터 정리. 반환 = 삭제된 앱 수 */
    suspend fun cleanup(retentionDays: Int): Int {        val before = System.currentTimeMillis() - retentionDays * TimeUtils.MILLIS_PER_DAY
        val deletedApps = db.appDao().deleteOlderThan(before)
        db.versionHistoryDao().deleteOlderThan(before)
        db.crawlLogDao().deleteOlderThan(before)
        db.notificationLogDao().deleteOlderThan(before)
        DebugLogger.i("정리", "보관 ${retentionDays}일 초과 정리: 앱 ${deletedApps}건")
        return deletedApps
    }
}

data class SaveResult(
    val total: Int,
    val created: Int,
    val updated: Int,
    val createdIds: List<String> = emptyList(),
)
