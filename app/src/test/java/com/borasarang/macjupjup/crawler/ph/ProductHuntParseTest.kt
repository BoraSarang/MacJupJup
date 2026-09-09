package com.borasarang.macjupjup.crawler.ph

import com.borasarang.macjupjup.data.db.entity.CrawlSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductHuntParseTest {

    private val source = CrawlSource(
        id = "t", name = "t", type = "PH_FEED", baseUrl = "https://www.producthunt.com",
        enabled = true, intervalHours = 6, intervalMinutes = 360,
        lastRunAt = null, lastStatus = "NEVER_RUN", errorMessage = null, selectorConfigJson = null,
    )

    private val atom = """
    <feed xmlns="http://www.w3.org/2005/Atom">
      <entry>
        <title>NotchNook — macOS notch utility</title>
        <link href="https://www.producthunt.com/posts/notchnook"/>
        <author><name>maker1</name></author>
        <summary>Turn your notch into a control center</summary>
        <category term="mac"/>
        <category term="productivity"/>
      </entry>
      <entry>
        <title>WebSaaS — browser analytics</title>
        <link href="https://www.producthunt.com/posts/websaas"/>
        <author><name>maker2</name></author>
        <summary>Web only dashboard</summary>
        <category term="saas"/>
      </entry>
    </feed>
    """.trimIndent()

    @Test
    fun `mac관련만_1건`() {
        val drafts = ProductHuntFeedCrawler(source).parseFeed(atom)
        assertEquals(1, drafts.size)
        assertEquals("NotchNook", drafts[0].app.name)
        assertEquals("FREE", drafts[0].app.license)
        assertTrue(drafts[0].app.tags?.contains("MenuBar") == true)
    }

    @Test
    fun `키워드판정`() {
        assertTrue(ProductHuntFeedCrawler.isMacRelated("macOS menu bar app"))
        assertFalse(ProductHuntFeedCrawler.isMacRelated("web analytics dashboard"))
    }
}
