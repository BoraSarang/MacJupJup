package com.borasarang.macjupjup.crawler.mmb

import com.borasarang.macjupjup.data.db.entity.CrawlSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MacMenuBarParseTest {

    private val source = CrawlSource(
        id = "t", name = "t", type = "MACMENUBAR", baseUrl = "https://macmenubar.com",
        enabled = true, intervalHours = 6, intervalMinutes = 360,
        lastRunAt = null, lastStatus = "NEVER_RUN", errorMessage = null, selectorConfigJson = null,
    )

    private val rss = """
    <rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/">
    <channel><title>MacMenuBar.com</title>
    <item>
      <title>StatusPerch</title>
      <link>https://macmenubar.com/statusperch/</link>
      <pubDate>Tue, 08 Sep 2026 12:52:59 +0000</pubDate>
      <category><![CDATA[Menu Bar Managers]]></category>
      <category><![CDATA[Free apps]]></category>
      <category><![CDATA[| Open Source]]></category>
      <description><![CDATA[Organize cluttered macOS status icons. Visit]]></description>
      <content:encoded><![CDATA[<p><img src="https://macmenubar.com/wp-content/uploads/2026/09/statusperch.webp" alt="StatusPerch"/></p><p>Organize cluttered macOS status icons.</p><p><a class="button" href="https://qingtan-labs.github.io/StatusPerch/">Visit</a></p>]]></content:encoded>
    </item>
    <item>
      <title>LinkFlick</title>
      <link>https://macmenubar.com/linkflick/</link>
      <pubDate>Tue, 08 Sep 2026 09:14:38 +0000</pubDate>
      <category><![CDATA[Keyboard &amp; Mouse Apps]]></category>
      <category><![CDATA[Paid apps]]></category>
      <description><![CDATA[Switches devices between Macs.]]></description>
      <content:encoded><![CDATA[<p>No image here.</p><p><a href="https://linkflick.com/">Visit</a></p>]]></content:encoded>
    </item>
    </channel></rss>
    """.trimIndent()

    @Test
    fun `rss_2건_라이선스_스크린샷_방문링크`() {
        val drafts = MacMenuBarCrawler(source).parseFeed(rss)
        assertEquals(2, drafts.size)
        val oss = drafts[0].app
        assertEquals("StatusPerch", oss.name)
        assertEquals("OSS", oss.license)
        assertTrue(oss.tags?.contains("MenuBar") == true)
        assertTrue(oss.screenshotUrls?.contains("statusperch.webp") == true)
        assertTrue(oss.descriptionSnippet?.contains("status icons") == true)
        assertTrue(!oss.descriptionSnippet!!.endsWith("Visit"))
        assertEquals("qingtan-labs/StatusPerch", oss.repoFullName)
        assertTrue(oss.releaseDate != null && oss.releaseDate > 0)
        val paid = drafts[1].app
        assertEquals("PAID", paid.license)
        assertNull(paid.screenshotUrls)
        assertEquals("https://linkflick.com/", paid.homepageUrl)
    }

    @Test
    fun `repo추정_githubio만`() {
        assertEquals(
            "yudaotor/lyrimuse",
            MacMenuBarCrawler.guessRepo("https://yudaotor.github.io/lyrimuse/"),
        )
        assertNull(MacMenuBarCrawler.guessRepo("https://linkflick.com/"))
        assertNull(MacMenuBarCrawler.guessRepo("not a url"))
    }
}
