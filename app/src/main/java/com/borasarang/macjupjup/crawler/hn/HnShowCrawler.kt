package com.borasarang.macjupjup.crawler.hn

import com.borasarang.macjupjup.crawler.AppDraft
import com.borasarang.macjupjup.crawler.BaseCrawler
import com.borasarang.macjupjup.crawler.str
import com.borasarang.macjupjup.crawler.splitTitle
import com.borasarang.macjupjup.data.db.entity.CrawlSource
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URLEncoder

/**
 * Hacker News Show HN 수집 (Algolia 공개 API, 키 불필요).
 * mac 관련 쿼리 2종 + traction(points + 댓글×2) 하한으로 노이즈 제거.
 * 외부 URL은 homepage로, HN 스레드는 출처 링크로 저장.
 */
class HnShowCrawler(
    source: CrawlSource,
    private val queries: List<String> = DEFAULT_QUERIES,
) : BaseCrawler(source) {

    override suspend fun crawl(): Result<List<AppDraft>> = runCatching {
        crawlEach(queries, "Show HN") { q ->
            val url = "https://hn.algolia.com/api/v1/search" +
                "?tags=show_hn&query=${URLEncoder.encode(q, "UTF-8")}&hitsPerPage=$HITS"
            parseHits(fetchGet(url))
        }
    }

    internal fun parseHits(body: String): List<AppDraft> {
        val hits = try {
            Json.parseToJsonElement(body).jsonObject["hits"]?.jsonArray ?: return emptyList()
        } catch (_: Exception) {
            parseFail("HN 응답")
        }
        return hits.mapNotNull { el ->
            try {
                val o = el.jsonObject
                val title = o.str("title") ?: return@mapNotNull null
                if (!isMacShow(title)) return@mapNotNull null
                val points = o["points"]?.jsonPrimitive?.int ?: 0
                val comments = o["num_comments"]?.jsonPrimitive?.int ?: 0
                val traction = points + comments * 2
                if (traction < MIN_TRACTION) return@mapNotNull null
                val objectId = o.str("objectID") ?: return@mapNotNull null
                val externalUrl = o.str("url")?.takeIf { it.isNotBlank() }
                val author = o.str("author") ?: "HN"
                // "Show HN: 이름 — 설명" 관례 분리 (공백 포함 구분자만)
                val name = splitTitle(
                    title.removePrefix("Show HN:").removePrefix("Show HN"),
                    listOf(": "),
                ).ifBlank { title }
                buildDraft(
                    name = name,
                    developer = "HN @$author",
                    homepageUrl = externalUrl,
                    descriptionSnippet = "Show HN (▲$points · 💬$comments) — $title",
                    detailUrl = "https://news.ycombinator.com/item?id=$objectId",
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    companion object {
        const val HITS = 50
        const val MIN_TRACTION = 3
        val DEFAULT_QUERIES = listOf("macos", "mac app")

        /** 공통 7종 외 HN 전용 키워드 */
        private val HN_EXTRA_KEYWORDS = setOf("swiftui", "appkit")

        /** 호환 유지: 기존 시그니처 (테스트용) */
        fun isMacShow(title: String): Boolean =
            com.borasarang.macjupjup.crawler.isMacRelated(title, HN_EXTRA_KEYWORDS)
    }
}
