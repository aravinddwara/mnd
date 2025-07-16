// File: plugins/tamildhool/TamildhoolPlugin.kt
package com.tamildhool

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.Jsoup

class TamildhoolPlugin : MainAPI() {
    override var mainUrl = "https://www.tamildhool.net"
    override var name = "TamilDhool"
    override val hasMainPage = true
    override val hasQuickSearch = false
    override val hasDownloadSupport = true
    override val supportedTypes = setOf(TvType.TvSeries)

    private val channelMap = mapOf(
        "Vijay TV" to "/vijay-tv/vijay-tv-serial/",
        "Sun TV" to "/sun-tv/sun-tv-serial/",
        "Zee Tamil" to "/zee-tamil/zee-tamil-serial/",
        "Kalaignar TV" to "/kalaignar-tv/kalaignar-tv-serial/"
    )

    override suspend fun getMainPage(): HomePageResponse {
        val home = channelMap.map { (name, path) ->
            val url = "$mainUrl$path"
            val doc = app.get(url).document

            val items = doc.select("article.post").mapNotNull {
                val link = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null
                val title = it.selectFirst("h2")?.text() ?: return@mapNotNull null
                TvSeriesSearchResponse(
                    title = title,
                    url = link,
                    apiName = this.name,
                    type = TvType.TvSeries,
                    posterUrl = it.selectFirst("img")?.attr("src")
                )
            }

            HomePageList(name, items)
        }
        return HomePageResponse(home)
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1")?.text() ?: url.substringAfterLast("/")

        val episodes = doc.select("article.post a").mapNotNull {
            val epUrl = it.attr("href")
            val epTitle = it.selectFirst("h2")?.text() ?: it.text()
            Episode(epUrl, epTitle)
        }

        return TvSeriesLoadResponse(
            name = title,
            url = url,
            apiName = this.name,
            type = TvType.TvSeries,
            episodes = episodes.reversed() // latest last
        )
    }

    override suspend fun loadLinks(episode: Episode): Boolean {
        val doc = app.get(episode.url).document
        val dailymotionId = doc.select("link[rel=prefetch]")
            .firstOrNull { it.attr("href").contains("dai.ly") }
            ?.attr("href")
            ?.substringAfterLast("/") ?: return false

        val dailymotionUrl = "https://www.dailymotion.com/video/$dailymotionId"
        loadExtractor(dailymotionUrl, episode.url, subtitleCallback, callback)
        return true
    }
}
