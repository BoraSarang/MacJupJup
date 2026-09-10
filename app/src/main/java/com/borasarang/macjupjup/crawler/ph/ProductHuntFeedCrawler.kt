package com.borasarang.macjupjup.crawler.ph

import com.borasarang.macjupjup.crawler.AppDraft
import com.borasarang.macjupjup.crawler.BaseCrawler
import com.borasarang.macjupjup.crawler.splitTitle
import com.borasarang.macjupjup.data.db.entity.CrawlSource
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
        crawlEach(feeds, "Product Hunt") { feed ->
            parseFeed(fetchGet(feed))
        }
    }

    internal fun parseFeed(body: String): List<AppDraft> {
        val doc = try {
            Jsoup.parse(body, "", Parser.xmlParser())
        } catch (_: Exception) {
            parseFail("PH 피드")
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
                if (!com.borasarang.macjupjup.crawler.isMacRelated("$title $summary ${categories.joinToString(" ")}", PH_EXTRA_KEYWORDS)) {
                    return@mapNotNull null
                }
                // PH 제목 관례: "이름 — 태그라인" (공백 포함 구분자만 분리)
                val name = splitTitle(title)
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
        // 토픽 피드 2종은 404로 폐쇄됨(2026-09 확인) — 전체 피드만 수집
        val DEFAULT_FEEDS = listOf(
            "https://www.producthunt.com/feed",
        )

        /** 공통 7종 외 PH 전용 키워드 */
        private val PH_EXTRA_KEYWORDS = setOf("apple silicon", "raycast", "macbook", "imac")

        /** 호환 유지: 기존 1인자 시그니처 (테스트·외부 호출용) */
        fun isMacRelated(text: String): Boolean =
            com.borasarang.macjupjup.crawler.isMacRelated(text, PH_EXTRA_KEYWORDS)
    }
}
