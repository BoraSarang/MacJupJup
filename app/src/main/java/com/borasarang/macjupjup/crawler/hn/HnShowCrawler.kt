package com.borasarang.macjupjup.crawler.hn

import com.borasarang.macjupjup.crawler.AppDraft
import com.borasarang.macjupjup.crawler.BaseCrawler
import com.borasarang.macjupjup.crawler.github.str
import com.borasarang.macjupjup.data.db.entity.CrawlSource
import com.borasarang.macjupjup.util.DebugLogger
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
        val drafts = mutableListOf<AppDraft>()
        for (q in queries) {
            val url = "https://hn.algolia.com/api/v1/search" +
                "?tags=show_hn&query=${URLEncoder.encode(q, "UTF-8")}&hitsPerPage=$HITS"
            val body = fetchGet(url)
            drafts += parseHits(body)
            politenessDelay()
        }
        val seen = mutableSetOf<String>()
        drafts.filter { seen.add(it.app.id) }.also {
            DebugLogger.i("수집", "Show HN 완료 queries=${queries.size} found=${drafts.size} unique=${it.size}")
        }
    }

    internal fun parseHits(body: String): List<AppDraft> {
        val hits = try {
            Json.parseToJsonElement(body).jsonObject["hits"]?.jsonArray ?: return emptyList()
        } catch (_: Exception) {
            throw IllegalStateException("HN 응답 파싱 실패 (E-AND-CRAWL-0201)")
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
                val name = title.removePrefix("Show HN:")
                    .removePrefix("Show HN")
                    .split(" — ", " – ", " | ", " - ", ": ").first().trim()
                    .ifBlank { title }
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

        private val MAC_KEYS = listOf("mac", "macos", "menu bar", "menubar", "swiftui", "mac app", "appkit", "notch")

        fun isMacShow(title: String): Boolean {
            val hay = " $title ".lowercase()
            return MAC_KEYS.any { hay.contains(it) }
        }
    }
}
