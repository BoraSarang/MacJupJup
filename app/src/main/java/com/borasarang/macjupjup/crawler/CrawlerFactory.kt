package com.borasarang.macjupjup.crawler

import com.borasarang.macjupjup.crawler.github.GitHubReleasesCrawler
import com.borasarang.macjupjup.crawler.github.GitHubSearchCrawler
import com.borasarang.macjupjup.crawler.hn.HnShowCrawler
import com.borasarang.macjupjup.crawler.itunes.ITunesLookupPoller
import com.borasarang.macjupjup.crawler.itunes.ITunesNameMatcher
import com.borasarang.macjupjup.crawler.mmb.MacMenuBarCrawler
import com.borasarang.macjupjup.crawler.chart.ChartRssCrawler
import com.borasarang.macjupjup.crawler.ph.ProductHuntFeedCrawler
import com.borasarang.macjupjup.crawler.setapp.SetappSeedCrawler
import com.borasarang.macjupjup.data.db.MacDatabase
import com.borasarang.macjupjup.data.db.entity.CrawlSource
import com.borasarang.macjupjup.util.Constants

/** 소스 type으로 크롤러 구현체 분기 (7종 전부 연결됨) */
class CrawlerFactory(
    private val db: MacDatabase,
    private val githubToken: String = "",
) {
    fun create(source: CrawlSource): AppCrawler {
        return when (source.type) {
            Constants.TYPE_GITHUB_SEARCH -> GitHubSearchCrawler(source, githubToken)
            Constants.TYPE_GITHUB_RELEASES -> GitHubReleasesCrawler(source, db, githubToken)
            Constants.TYPE_PH_FEED -> ProductHuntFeedCrawler(source)
            Constants.TYPE_HN_SHOW -> HnShowCrawler(source)
            Constants.TYPE_SETAPP_SEED -> SetappSeedCrawler(source, db)
            Constants.TYPE_CHART_RSS -> ChartRssCrawler(source)
            Constants.TYPE_ITUNES_LOOKUP -> ITunesLookupPoller(source, db)
            Constants.TYPE_NAME_MATCH -> ITunesNameMatcher(source, db)
            Constants.TYPE_MACMENUBAR -> MacMenuBarCrawler(source)
            else -> throw IllegalArgumentException("미지원 소스 type=${source.type}")
        }
    }
}
