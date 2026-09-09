package com.borasarang.macjupjup.crawler.mmb

import com.borasarang.macjupjup.crawler.AppDraft
import com.borasarang.macjupjup.crawler.BaseCrawler
import com.borasarang.macjupjup.data.db.entity.CrawlSource
import com.borasarang.macjupjup.util.AppTag
import com.borasarang.macjupjup.util.CategoryInfer
import com.borasarang.macjupjup.util.Constants
import com.borasarang.macjupjup.util.DebugLogger
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * MacMenuBar.com WordPress RSS 수집 (6h).
 * item: title/link/pubDate/categories/description/content:encoded.
 * - 라이선스: "| Open Source" → OSS, "Paid apps" → PAID, 그 외 FREE
 * - 스크린샷: content 첫 img, 홈페이지: Visit 링크
 * - github.io Visit 링크는 owner/repo 추정 (릴리즈 추적용, 404 시 스킵)
 * - 전원 메뉴바 앱 → MenuBar 태그 강제
 */
class MacMenuBarCrawler(
    source: CrawlSource,
    private val feeds: List<String> = DEFAULT_FEEDS,
) : BaseCrawler(source) {

    override suspend fun crawl(): Result<List<AppDraft>> = runCatching {
        val drafts = mutableListOf<AppDraft>()
        for (feed in feeds) {
            try {
                drafts += parseFeed(fetchGet(feed))
            } catch (e: Exception) {
                DebugLogger.w("수집", "MacMenuBar 피드 스킵 $feed: ${e.message}")
            }
            politenessDelay()
        }
        val seen = mutableSetOf<String>()
        drafts.filter { seen.add(it.app.id) }.also {
            DebugLogger.i("수집", "MacMenuBar 완료 feeds=${feeds.size} found=${drafts.size} unique=${it.size}")
        }
    }

    internal fun parseFeed(body: String): List<AppDraft> {
        val doc = try {
            Jsoup.parse(body, "", Parser.xmlParser())
        } catch (_: Exception) {
            throw IllegalStateException("MacMenuBar 피드 파싱 실패 (E-AND-CRAWL-0201)")
        }
        return doc.select("item").mapNotNull { e ->
            try {
                val title = e.selectFirst("title")?.text()?.trim() ?: return@mapNotNull null
                val link = e.selectFirst("link")?.text()?.trim() ?: return@mapNotNull null
                val pubDate = e.selectFirst("pubDate")?.text()?.trim()?.let { parseRssDate(it) }
                val categories = e.select("category").map { it.text().trim() }
                val rawDesc = e.selectFirst("description")?.text()?.trim()
                val content = e.selectFirst("content|encoded")?.text()
                    ?: e.getElementsByTag("content:encoded").firstOrNull()?.text()
                val description = cleanTail(rawDesc)
                val license = licenseOf(categories)
                val shots = content?.let { Jsoup.parseBodyFragment(it).select("img[src]") }
                    ?.map { it.attr("abs:src").ifBlank { it.attr("src") } }
                    ?.filter { it.isNotBlank() && it.startsWith("http") } ?: emptyList()
                val homepage = content?.let { Jsoup.parseBodyFragment(it) }
                    ?.select("a[href]")?.map { it.attr("abs:href").ifBlank { it.attr("href") } }
                    ?.firstOrNull { u ->
                        u.isNotBlank() && !u.contains("macmenubar.com") &&
                            !u.matches(Regex(".*\\.(png|jpg|jpeg|webp|gif|svg)(\\?.*)?"))
                    }
                val repo = homepage?.let { guessRepo(it) }
                val draft = buildDraft(
                    name = title,
                    developer = "MacMenuBar",
                    repoFullName = repo,
                    homepageUrl = homepage,
                    descriptionSnippet = description,
                    detailUrl = link,
                    releaseDate = pubDate,
                ) ?: return@mapNotNull null
                val inferred = CategoryInfer.infer(
                    "$title ${categories.firstOrNull { !isLicenseCat(it) } ?: ""} ${description ?: ""}",
                )
                val tags = (inferred.tags + AppTag.MENU_BAR).distinct()
                val app = draft.app.copy(
                    license = license,
                    category = inferred.category,
                    tags = tags.joinToString(","),
                    screenshotUrls = shots.ifEmpty { null }?.joinToString("\n"),
                )
                draft.copy(app = app)
            } catch (_: Exception) {
                null
            }
        }
    }

    companion object {
        val DEFAULT_FEEDS = listOf("https://macmenubar.com/feed/")

        fun isLicenseCat(c: String): Boolean {
            val t = c.trim()
            return t.equals("Free apps", ignoreCase = true) ||
                t.equals("Paid apps", ignoreCase = true) ||
                t.equals("Freemium apps", ignoreCase = true) ||
                t.contains("Open Source", ignoreCase = true)
        }

        fun licenseOf(categories: List<String>): String {
            if (categories.any { it.contains("Open Source", ignoreCase = true) }) {
                return Constants.LICENSE_OSS
            }
            if (categories.any { it.equals("Paid apps", ignoreCase = true) }) {
                return Constants.LICENSE_PAID
            }
            return Constants.LICENSE_FREE
        }

        /** 설명 끝 "Visit"/"Watch" 잔재 제거 */
        fun cleanTail(desc: String?): String? {
            if (desc.isNullOrBlank()) return null
            return desc.replace(Regex("\\s*(Visit|Watch)\\s*$"), "").trim().ifBlank { null }
        }

        /** owner.github.io/repo 형태 Visit 링크 → owner/repo 추정 */
        fun guessRepo(url: String): String? {
            return try {
                val u = java.net.URI(url)
                val host = u.host ?: return null
                if (!host.endsWith(".github.io")) return null
                val owner = host.removeSuffix(".github.io")
                val repo = u.path.trim('/').substringBefore('/').ifBlank { return null }
                if (owner.isBlank() || repo.isBlank()) return null
                "$owner/$repo"
            } catch (_: Exception) {
                null
            }
        }

        fun parseRssDate(s: String): Long? = try {
            ZonedDateTime.parse(s, DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.US))
                .toInstant().toEpochMilli()
        } catch (_: Exception) {
            null
        }
    }
}
