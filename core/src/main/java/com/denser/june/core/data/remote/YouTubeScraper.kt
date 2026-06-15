package com.denser.june.core.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.util.regex.Pattern

class YouTubeScraper(private val client: OkHttpClient) {

    init {
        if (!isInitialized.get()) {
            try {
                NewPipe.init(NewPipeDownloader(client))
                isInitialized.set(true)
            } catch (e: Exception) {
                Log.e("YouTubeScraper", "Failed to initialize NewPipe", e)
            }
        }
    }

    suspend fun fetchAudioStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        val url = "https://www.youtube.com/watch?v=$videoId"
        Log.i("YouTubeScraper", "Fetching audio stream for videoId: $videoId")
        try {
            val service = ServiceList.YouTube
            val extractor = service.getStreamExtractor(url)
            extractor.fetchPage()
            
            val audioStreams = extractor.audioStreams
            val bestStream = audioStreams.maxByOrNull { it.bitrate }
            
            if (bestStream != null) {
                Log.i("YouTubeScraper", "Found audio stream: ${bestStream.url}")
                return@withContext bestStream.url
            }
        } catch (e: Exception) {
            Log.e("YouTubeScraper", "Error fetching audio stream for $videoId", e)
        }
        null
    }

    suspend fun searchVideoId(query: String): String? = withContext(Dispatchers.IO) {
        Log.i("YouTubeScraper", "Searching YouTube for: $query")
        try {
            val service = ServiceList.YouTube
            val searchExtractor = service.getSearchExtractor(query)
            searchExtractor.fetchPage()
            
            val firstResult = searchExtractor.initialPage.items.filterIsInstance<StreamInfoItem>().firstOrNull()
            val videoId = firstResult?.url?.let { extractVideoId(it) }

            if (videoId != null) {
                Log.i("YouTubeScraper", "Found videoId via search: $videoId")
                return@withContext videoId
            }
        } catch (e: Exception) {
            Log.e("YouTubeScraper", "Search failed for: $query", e)
        }
        null
    }

    fun extractVideoId(url: String): String? {
        if (url.length == 11 && !url.contains("/") && !url.contains("?")) return url
        val pattern = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*"
        val compiledPattern = Pattern.compile(pattern)
        val matcher = compiledPattern.matcher(url)
        return if (matcher.find()) matcher.group() else {
            val uri = android.net.Uri.parse(url)
            uri.getQueryParameter("v") ?: url.split("/").lastOrNull()?.takeIf { it.length == 11 }
        }
    }

    companion object {
        private val isInitialized = java.util.concurrent.atomic.AtomicBoolean(false)
    }
}
