package com.borasarang.macjupjup.crawler.setapp

import com.borasarang.macjupjup.data.db.entity.CrawlSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetappParseTest {

    private val source = CrawlSource(
        id = "t", name = "t", type = "SETAPP_SEED", baseUrl = "https://setapp.com",
        enabled = false, intervalHours = 720, intervalMinutes = 43200,
        lastRunAt = null, lastStatus = "DISABLED", errorMessage = null, selectorConfigJson = null,
    )

    private val html = """
    <html><body>
    <a class="application-card_x" href="/apps/aldente-pro"><h3>AlDente Pro</h3>
      <div>Set battery charging limits</div><span>98</span><span>%</span><span>•</span><span>Mac</span></a>
    <a class="application-card_x" href="/apps/wrapped"><div><h3>Wrapped</h3>
      <div>Outer wrapper 98% Mac text</div><div>Real tagline here</div></div><span>Mac</span></a>
    <a class="application-card_x" href="/apps/ios-only"><h3>iOS Only</h3>
      <div>iPhone tool</div><span>iOS</span></a>
    <a class="application-card_x" href="/apps/aldente-pro"><h3>AlDente Pro</h3>
      <div>dup</div><span>Mac</span></a>
    </body></html>
    """.trimIndent()

    @Test
    fun `상세_og설명_아이콘_스크린샷`() {
        val html = """
        <html><head>
        <meta property="og:description" content="Optimize battery charging."/>
        </head><body>
        <img src="https://store.setapp.com/app/1/2/icon-abc.png"/>
        <img src="https://store.setapp.com/app/1/screenshots/a.png"/>
        <img src="https://store.setapp.com/app/1/screenshots/b.png"/>
        </body></html>
        """.trimIndent()
        val detail = SetappSeedCrawler(source).parseDetail(html)
        assertEquals("Optimize battery charging.", detail.description)
        assertTrue(detail.icon?.contains("/icon-") == true)
        assertEquals(2, detail.screenshots.size)
    }

    @Test
    fun `Mac카드만_중복제거`() {
        val drafts = SetappSeedCrawler(source).parseListing(html)
        val names = drafts.map { it.app.name }
        assertTrue(names.contains("AlDente Pro"))
        assertTrue(names.contains("Wrapped"))
        assertEquals(2, drafts.size)
        val aldente = drafts.first { it.app.name == "AlDente Pro" }
        assertTrue(aldente.app.descriptionSnippet?.contains("battery") == true)
        // 래퍼 오염 방지: % 포함 텍스트는 태그라인이 아님
        val wrapped = drafts.first { it.app.name == "Wrapped" }
        assertEquals("Real tagline here", wrapped.app.descriptionSnippet)
    }
}
