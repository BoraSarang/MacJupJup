package com.borasarang.macjupjup.crawler.ph

import com.borasarang.macjupjup.crawler.AppDraft
import com.borasarang.macjupjup.crawler.BaseCrawler
import com.borasarang.macjupjup.data.db.entity.CrawlSource
import com.borasarang.macjupjup.util.DebugLogger
import org.jsoup.Jsoup
import org.jsoup.parser.Parser

/**
 * Product Hunt 공개 Atom 피드 수집 (무인증).
 * 피드: /feed + topics/mac/feed + topics/developer-tools/feed.
 * mac 관련 키워드 필터 후 이름·태그라인·링크만 저장 (상세 스크랩 금지).
 * 라이선스는 FREE 기본값 (유료 여부는 스토어 대조·수동 오버라이드로 보정).
 */
class ProductHuntFeedCrawler(
    source: CrawlSource,
    private val feeds: List<String> = DEFAULT_FEEDS,
) : BaseCrawler(source) {

    override suspend fun crawl(): Result<List<AppDraft>> = runCatching {
        val drafts = mutableListOf<AppDraft>()
        for (feed in feeds) {
            try {
                val body = fetchGet(feed)
                drafts += parseFeed(body)
            } catch (e: Exception) {
                DebugLogger.w("수집", "PH 피드 스킵 $feed: ${e.message}")
            }
            politenessDelay()
        }
        val seen = mutableSetOf<String>()
        drafts.filter { seen.add(it.app.id) }.also {
            DebugLogger.i("수집", "Product Hunt 완료 feeds=${feeds.size} found=${drafts.size} unique=${it.size}")
        }
    }

    internal fun parseFeed(body: String): List<AppDraft> {
        val doc = try {
            Jsoup.parse(body, "", Parser.xmlParser())
        } catch (_: Exception) {
            throw IllegalStateException("PH 피드 파싱 실패 (E-AND-CRAWL-0201)")
        }
        return doc.select("entry").mapNotNull { e ->
            try {
                val title = e.selectFirst("title")?.text()?.trim() ?: return@mapNotNull null
                val link = e.selectFirst("link")?.attr("href")?.trim() ?: return@mapNotNull null
                val summary = e.selectFirst("summary")?.text()?.trim()
                    ?: e.selectFirst("content")?.text()?.trim()
                val author = e.selectFirst("author > name")?.text()?.trim().orEmpty()
                val categories = e.select("category").map { it.attr("term").trim() }
                    .filter { it.isNotBlank() }
                if (!isMacRelated("$title $summary ${categories.joinToString(" ")}")) {
                    return@mapNotNull null
                }
                // PH 제목 관례: "이름 — 태그라인" (공백 포함 구분자만 분리)
                val name = title.split(" — ", " – ", " | ", " - ").first().trim().ifBlank { title }
                buildDraft(
                    name = name,
                    developer = author.ifBlank { "Product Hunt" },
                    descriptionSnippet = summary,
                    topics = categories,
                    detailUrl = link,
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    companion object {
        val DEFAULT_FEEDS = listOf(
            "https://www.producthunt.com/feed",
            "https://www.producthunt.com/topics/mac/feed",
            "https://www.producthunt.com/topics/developer-tools/feed",
        )

        private val MAC_KEYWORDS = listOf(
            "mac", "macos", "mac os", "menu bar", "menubar", "mac app",
            "apple silicon", "notch", "raycast", "macbook", "imac",
        )

        fun isMacRelated(text: String): Boolean {
            val hay = " $text ".lowercase()
            return MAC_KEYWORDS.any { hay.contains(it) }
        }
    }
}
