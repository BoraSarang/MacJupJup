package com.borasarang.macjupjup.crawler.github

import com.borasarang.macjupjup.data.db.entity.CrawlSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubParseTest {

    private val source = CrawlSource(
        id = "t", name = "t", type = "GITHUB_SEARCH", baseUrl = "https://api.github.com",
        enabled = true, intervalHours = 6, intervalMinutes = 360,
        lastRunAt = null, lastStatus = "NEVER_RUN", errorMessage = null, selectorConfigJson = null,
    )

    private val searchJson = """
    {"total_count":2,"items":[
      {"full_name":"owner/RaycastClone","name":"RaycastClone",
       "owner":{"login":"owner"},"description":"macOS launcher",
       "stargazers_count":120,"language":"Swift","topics":["macos","launcher"],
       "html_url":"https://github.com/owner/RaycastClone","homepage":"",
       "pushed_at":"2026-09-01T00:00:00Z","default_branch":"main"},
      {"full_name":"o/NoDesc","name":"NoDesc","owner":{"login":"o"},
       "description":null,"stargazers_count":5,"language":null,"topics":[],
       "html_url":"https://github.com/o/NoDesc","homepage":null,
       "pushed_at":"bad-date","default_branch":"main"}
    ]}
    """.trimIndent()

    @Test
    fun `search_2건파싱_null내성`() {
        val drafts = GitHubSearchCrawler(source).parseRepos(searchJson)
        assertEquals(2, drafts.size)
        val first = drafts[0].app
        assertEquals("owner/RaycastClone", first.repoFullName)
        assertEquals("OSS", first.license)
        assertEquals(120, first.stars)
        assertEquals("Swift", first.primaryLanguage)
        assertEquals("생산성", first.category)
        assertNotNull(drafts[1].app)
    }

    @Test
    fun `releases_최신태그`() {
        val body = """[{"tag_name":"v1.2.0","draft":false,
          "body":"새 기능 추가","html_url":"https://github.com/o/r/releases/tag/v1.2.0"},
          {"tag_name":"v1.1.0","draft":false,"body":"이전","html_url":"x"}]"""
        val latest = GitHubReleasesCrawler(source, { emptyList() }).parseLatestRelease(body)
        assertNotNull(latest)
        assertEquals("v1.2.0", latest!!.tag)
        assertEquals("새 기능 추가", latest.notes)
    }

    @Test
    fun `releases_없으면_null`() {
        val latest = GitHubReleasesCrawler(source, { emptyList() }).parseLatestRelease("[]")
        assertNull(latest)
    }

    @Test
    fun `epoch_파싱`() {
        assertTrue((GitHubSearchCrawler.parseEpoch("2026-09-01T00:00:00Z") ?: 0) > 0)
        assertNull(GitHubSearchCrawler.parseEpoch("bad-date"))
    }
}
