package com.borasarang.macjupjup.crawler

import com.borasarang.macjupjup.data.db.entity.App
import com.borasarang.macjupjup.data.db.entity.AppSourceMapping
import com.borasarang.macjupjup.data.db.entity.CrawlSource
import com.borasarang.macjupjup.util.AppLicense
import com.borasarang.macjupjup.util.CategoryInfer
import com.borasarang.macjupjup.util.Constants
import com.borasarang.macjupjup.util.MergeUtils
import com.borasarang.macjupjup.util.classifyLicense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * 크롤러 공통 베이스. 네트워크 I/O는 IO 디스패처, 요청 간 예의 대기.
 * 한 크롤러의 실패는 예외로 상위에 전달 → Worker가 격리 처리.
 */
abstract class BaseCrawler(
    protected val source: CrawlSource,
) : AppCrawler {

    override val sourceName: String get() = source.name

    protected suspend fun fetchGet(url: String): String = withContext(Dispatchers.IO) {
        val result = CrawlHttp.get(url)
        if (!result.isOk) {
            throw IllegalStateException("GET 실패 url=$url code=${result.code} err=${result.error}")
        }
        result.body
    }

    protected suspend fun fetchGetHeaders(url: String, headers: Map<String, String>): String =
        withContext(Dispatchers.IO) {
            val result = CrawlHttp.getWithHeaders(url, headers)
            if (!result.isOk) {
                throw IllegalStateException("GET 실패 url=$url code=${result.code} err=${result.error}")
            }
            result.body
        }

    protected suspend fun politenessDelay() {
        delay(Constants.CRAWL_REQUEST_DELAY_MS)
    }

    /**
     * 크롤러 공통 App 초안 빌더. 라이선스 자동분류 + 카테고리/태그 추론 적용.
     * 수동 오버라이드(licenseOverride)가 있으면 M5 설정에서 별도 처리.
     */
    protected fun buildDraft(
        name: String,
        developer: String,
        price: Double = 0.0,
        repoFullName: String? = null,
        homepageUrl: String? = null,
        version: String? = null,
        releaseNotesSummary: String? = null,
        releaseNotes: String? = null,
        releaseDate: Long? = null,
        descriptionSnippet: String? = null,
        trackId: Long? = null,
        averageRating: Double? = null,
        ratingCount: Int? = null,
        stars: Int? = null,
        primaryLanguage: String? = null,
        topics: List<String> = emptyList(),
        iconUrl: String? = null,
        forks: Int? = null,
        issues: Int? = null,
        licenseName: String? = null,
        detailUrl: String,
    ): AppDraft? {
        if (name.isBlank() || developer.isBlank()) return null
        val id = MergeUtils.generateId(name, developer)
        val now = System.currentTimeMillis()
        val inferred = CategoryInfer.infer("$name $developer ${descriptionSnippet ?: ""}", topics)
        val license = when (classifyLicense(repoFullName, price)) {
            AppLicense.OSS -> Constants.LICENSE_OSS
            AppLicense.PAID -> Constants.LICENSE_PAID
            AppLicense.FREE -> Constants.LICENSE_FREE
        }
        val app = App(
            id = id,
            platform = Constants.PLATFORM_MACOS,
            name = name.trim(),
            developer = developer.trim(),
            license = license,
            price = price,
            currency = "USD",
            category = inferred.category,
            tags = inferred.tags.joinToString(",").ifBlank { null },
            trackId = trackId,
            repoFullName = repoFullName,
            homepageUrl = homepageUrl,
            version = version,
            prevVersion = null,
            releaseNotesSummary = releaseNotesSummary?.take(500),
            releaseNotes = releaseNotes?.take(2000),
            releaseDate = releaseDate,
            descriptionSnippet = descriptionSnippet?.take(2000),
            screenshotUrls = null,
            averageRating = averageRating,
            ratingCount = ratingCount,
            stars = stars,
            primaryLanguage = primaryLanguage,
            topics = topics.joinToString(",").ifBlank { null },
            iconUrl = iconUrl,
            descriptionKo = null,
            releaseNotesKo = null,
            sellerName = null,
            fileSize = null,
            minOs = null,
            contentRating = null,
            forks = forks,
            issues = issues,
            licenseName = licenseName,
            firstSeenAt = now,
            lastUpdatedAt = now,
            isNew = true,
            sourceId = source.id,
            licenseOverride = null,
        )
        val mapping = AppSourceMapping(
            appId = id,
            sourceName = source.name,
            sourceUrl = detailUrl,
            fetchedAt = now,
        )
        return AppDraft(app, listOf(mapping))
    }
}
