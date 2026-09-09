package com.borasarang.macjupjup.crawler.hn

import com.borasarang.macjupjup.data.db.entity.CrawlSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HnShowParseTest {

    private val source = CrawlSource(
        id = "t", name = "t", type = "HN_SHOW", baseUrl = "https://hn.algolia.com",
        enabled = true, intervalHours = 6, intervalMinutes = 360,
        lastRunAt = null, lastStatus = "NEVER_RUN", errorMessage = null, selectorConfigJson = null,
    )

    private val hits = """
    {"hits":[
      {"title":"Show HN: MenubarX – macOS menu bar browser","url":"https://example.com",
       "points":50,"num_comments":20,"objectID":"123","author":"dev1"},
      {"title":"Show HN: Web thing","url":"https://example.org",
       "points":200,"num_comments":100,"objectID":"124","author":"dev2"},
      {"title":"Show HN: Tiny mac util","url":null,
       "points":1,"num_comments":0,"objectID":"125","author":"dev3"}
    ]}
    """.trimIndent()

    @Test
    fun `mac+traction_1건`() {
        val drafts = HnShowCrawler(source).parseHits(hits)
        assertEquals(1, drafts.size)
        assertEquals("MenubarX", drafts[0].app.name)
        assertTrue(drafts[0].app.homepageUrl == "https://example.com")
    }
}
