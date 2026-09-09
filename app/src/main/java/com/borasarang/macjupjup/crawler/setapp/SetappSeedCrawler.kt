package com.borasarang.macjupjup.crawler.setapp

import com.borasarang.macjupjup.crawler.AppDraft
import com.borasarang.macjupjup.crawler.AppSourceMappingHelper
import com.borasarang.macjupjup.crawler.BaseCrawler
import com.borasarang.macjupjup.data.db.entity.CrawlSource
import com.borasarang.macjupjup.util.DebugLogger
import org.jsoup.Jsoup

/**
 * Setapp 공개 카탈로그 월간 시드 (https://setapp.com/apps, SSR).
 * 카드: a[href^=/apps/] → h3=이름, div=태그라인, Mac 표기 필터.
 * 상세 정보(버전·가격·스크린샷)는 iTunesLookupPoller가 이름 대조로 보완.
 * 과도 재수집 금지 — 월 1회.
 */
class SetappSeedCrawler(
    source: CrawlSource,
    private val listUrl: String = "https://setapp.com/apps",
    private val detailProvider: (suspend () -> List<com.borasarang.macjupjup.data.db.entity.App>)? = null,
    private val detailLimit: Int = 50,
) : BaseCrawler(source) {

    /** Room 기반 편의 생성자 (상세 보강용) */
    constructor(
        source: CrawlSource,
        db: com.borasarang.macjupjup.data.db.MacDatabase,
    ) : this(source, "https://setapp.com/apps", { db.appDao().getSetappWithoutIcon(50) }, 50)

    override suspend fun crawl(): Result<List<AppDraft>> = runCatching {
        val body = fetchGet(listUrl)
        val listed = parseListing(body)
        DebugLogger.i("수집", "Setapp 시드 완료 found=${listed.size}")
        val enriched = enrichDetails(listed)
        listed + enriched
    }

    /**
     * 상세 페이지 보강: og:description + 아이콘 + 스크린샷.
     * 월간 실행, 아이콘 없는 앱만, 실행당 50개 상한.
     */
    private suspend fun enrichDetails(listed: List<AppDraft>): List<AppDraft> {
        val provider = detailProvider ?: return emptyList()
        val targets = try {
            provider()
        } catch (_: Exception) {
            return emptyList()
        }.take(detailLimit)
        if (targets.isEmpty()) return emptyList()
        val byId = listed.associateBy { it.app.id }
        val out = mutableListOf<AppDraft>()
        for (app in targets) {
            // 목록에서 같은 id의 정확한 상세 URL 사용 (id 정규화로 slug 복원 불가)
            val slug = byId[app.id]?.mappings?.firstOrNull()?.sourceUrl ?: continue
            try {
                val detail = parseDetail(fetchGet(slug))
                val now = System.currentTimeMillis()
                out += AppDraft(
                    app.copy(
                        descriptionSnippet = detail.description ?: app.descriptionSnippet,
                        iconUrl = detail.icon ?: app.iconUrl,
                        screenshotUrls = detail.screenshots.ifEmpty { null }?.joinToString("\n")
                            ?: app.screenshotUrls,
                        lastUpdatedAt = now,
                        isNew = false,
                    ),
                    listOf(
                        AppSourceMappingHelper.mapping(
                            appId = app.id,
                            sourceName = source.name,
                            sourceUrl = slug,
                            now = now,
                        ),
                    ),
                )
            } catch (e: Exception) {
                DebugLogger.d("수집", "Setapp 상세 스킵 ${app.name}: ${e.message}")
            }
            politenessDelay()
        }
        DebugLogger.i("수집", "Setapp 상세 보강 완료 ${out.size}/${targets.size}")
        return out
    }

    data class AppDetail(
        val description: String?,
        val icon: String?,
        val screenshots: List<String>,
    )

    internal fun parseDetail(body: String): AppDetail {
        val doc = try {
            Jsoup.parse(body, "https://setapp.com")
        } catch (_: Exception) {
            throw IllegalStateException("Setapp 상세 파싱 실패 (E-AND-CRAWL-0201)")
        }
        val desc = doc.selectFirst("meta[property=og:description]")?.attr("content")?.trim()
            ?: doc.selectFirst("meta[name=description]")?.attr("content")?.trim()
        val icon = doc.select("img[src]").map { it.absUrl("src") }
            .firstOrNull { it.contains("/icon-") }
        val shots = doc.select("img[src]").map { it.absUrl("src") }
            .filter { it.contains("/screenshots/") }
            .distinct().take(6)
        return AppDetail(desc?.ifBlank { null }, icon, shots)
    }

    internal fun parseListing(body: String): List<AppDraft> {
        val doc = try {
            Jsoup.parse(body, "https://setapp.com")
        } catch (_: Exception) {
            throw IllegalStateException("Setapp 목록 파싱 실패 (E-AND-CRAWL-0201)")
        }
        val seen = mutableSetOf<String>()
        return doc.select("a[href]").mapNotNull { a ->
            try {
                val href = a.attr("href").trim()
                if (!href.matches(Regex("^/apps/[a-z0-9-]+$"))) return@mapNotNull null
                if (!seen.add(href)) return@mapNotNull null
                val text = a.text()
                // iOS 전용 제외 (Mac 표기 없는 카드 스킵)
                if (!text.contains("Mac")) return@mapNotNull null
                val name = a.selectFirst("h3")?.text()?.trim() ?: return@mapNotNull null
                // 태그라인: 바깥 래퍼 div(평점·Mac 포함 전체 텍스트) 제외하고
                // 4~120자 일반 div 중 첫 번째
                val tagline = a.select("div")
                    .map { it.text().trim() }
                    .filter { it.isNotEmpty() && it != name }
                    .firstOrNull { it.length in 4..120 && !it.contains("%") }
                val draft = buildDraft(
                    name = name,
                    developer = "Setapp",
                    descriptionSnippet = tagline,
                    detailUrl = "https://setapp.com$href",
                ) ?: return@mapNotNull null
                // Setapp 입점 = 상용 앱. repo 발견·lookup 보완 시 재분류될 수 있음
                draft.copy(
                    app = draft.app.copy(
                        license = com.borasarang.macjupjup.util.Constants.LICENSE_PAID,
                    )
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
