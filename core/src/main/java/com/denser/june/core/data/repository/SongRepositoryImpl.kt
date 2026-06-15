package com.denser.june.core.data.repository

import com.denser.june.core.data.mappers.mapSonglinkResponseToSongDetails
import com.denser.june.core.data.remote.SonglinkApiService
import com.denser.june.core.data.remote.SpotifyScraper
import com.denser.june.core.data.remote.YouTubeScraper
import com.denser.june.core.domain.repository.SongRepository
import com.denser.june.core.domain.model.SongDetails
import com.denser.june.core.domain.model.PlatformLinks
import com.denser.june.core.domain.preferences.PrivacyPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import android.util.Log

@Serializable
private data class ITunesSearchResponse(
    val resultCount: Int,
    val results: List<ITunesSearchResult>
)

@Serializable
private data class ITunesSearchResult(
    val trackName: String? = null,
    val artistName: String? = null,
    val artworkUrl100: String? = null,
    val previewUrl: String? = null,
    val trackViewUrl: String? = null
)

class SongRepositoryImpl(
    private val apiService: SonglinkApiService,
    private val spotifyScraper: SpotifyScraper,
    private val youtubeScraper: YouTubeScraper,
    private val client: OkHttpClient
) : SongRepository {

    override suspend fun fetchSongDetails(url: String): Result<SongDetails> {
        Log.i("SongRepository", "Fetching details for URL: $url")
        return try {
            val response = apiService.getSongLinks(url)
            var details = mapSonglinkResponseToSongDetails(response)
                ?: return Result.failure(Exception("Could not parse song details"))
            
            Log.i("SongRepository", "Mapped details: ${details.title} by ${details.artistName}")

            val spotifyId = response.linksByPlatform["spotify"]
                ?.entityUniqueId
                ?.split("::")
                ?.lastOrNull()

            if (spotifyId != null) {
                Log.d("SongRepository", "Attempting Spotify preview for ID: $spotifyId")
                val previewUrl = spotifyScraper.fetchPreviewUrl(spotifyId)
                if (!previewUrl.isNullOrBlank()) {
                    Log.i("SongRepository", "Found Spotify preview: $previewUrl")
                    details = details.copy(previewUrl = previewUrl)
                }
            }

            if (details.previewUrl.isNullOrBlank()) {
                val youtubeUrl = details.links.youtube ?: details.links.youtubeMusic
                Log.i("SongRepository", "No Spotify preview. Trying YouTube URL: $youtubeUrl")
                if (youtubeUrl != null) {
                    val videoId = youtubeScraper.extractVideoId(youtubeUrl)
                    if (videoId != null) {
                        val youtubeStream = youtubeScraper.fetchAudioStreamUrl(videoId)
                        if (youtubeStream != null) {
                            Log.i("SongRepository", "Found YouTube audio stream: $youtubeStream")
                            details = details.copy(previewUrl = youtubeStream)
                        }
                    }
                }
            }

            if (details.previewUrl.isNullOrBlank()) {
                Log.d("SongRepository", "No YouTube stream. Trying YouTube Search fallback.")
                val searchQuery = "${details.artistName} ${details.title}"
                var searchedVideoId = youtubeScraper.searchVideoId(searchQuery)
                
                if (searchedVideoId == null) {
                    Log.i("SongRepository", "Artist + Title search failed. Trying Title only.")
                    searchedVideoId = youtubeScraper.searchVideoId(details.title)
                }

                if (searchedVideoId != null) {
                    val searchedStream = youtubeScraper.fetchAudioStreamUrl(searchedVideoId)
                    if (searchedStream != null) {
                        Log.i("SongRepository", "Found stream from YouTube Search: $searchedStream")
                        details = details.copy(previewUrl = searchedStream)
                    }
                }
            }

            if (details.previewUrl.isNullOrBlank()) {
                Log.d("SongRepository", "No YouTube search result. Trying iTunes fallback with Artist + Title.")
                val searchQuery = "${details.artistName} ${details.title}"
                var fallbackPreview = fetchItunesPreviewUrl(searchQuery)
                
                if (fallbackPreview == null) {
                    Log.d("SongRepository", "iTunes Artist+Title failed. Trying Title only.")
                    fallbackPreview = fetchItunesPreviewUrl(details.title)
                }

                if (fallbackPreview != null) {
                    Log.i("SongRepository", "Found iTunes fallback: $fallbackPreview")
                    details = details.copy(previewUrl = fallbackPreview)
                }
            }

            if (details.previewUrl.isNullOrBlank()) {
                Log.w("SongRepository", "No playable preview found from any source.")
            }

            Result.success(details)
        } catch (e: Exception) {
            Log.e("SongRepository", "Error fetching song details", e)
            Result.failure(e)
        }
    }

    private suspend fun fetchItunesPreviewUrl(query: String): String? {
        Log.d("SongRepository", "Searching iTunes for preview: $query")
        return try {
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "https://itunes.apple.com/search?term=$encodedQuery&media=music&limit=3"
            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w("SongRepository", "iTunes search failed with code: ${response.code}")
                    return null
                }
                val body = response.body?.string() ?: return null

                val json = Json { ignoreUnknownKeys = true }
                val searchResponse = json.decodeFromString<ITunesSearchResponse>(body)
                val preview = searchResponse.results.firstOrNull { !it.previewUrl.isNullOrBlank() }?.previewUrl
                
                if (preview != null) {
                    val finalUrl = preview.replace("http://", "https://")
                    Log.i("SongRepository", "Found iTunes preview URL: $finalUrl")
                    finalUrl
                } else {
                    Log.d("SongRepository", "No preview URL found in iTunes results for: $query")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("SongRepository", "Error searching iTunes", e)
            null
        }
    }

    override suspend fun searchSongs(query: String): Result<List<SongDetails>> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "https://itunes.apple.com/search?term=$encodedQuery&media=music&limit=10"
            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext Result.failure(Exception("Failed to search iTunes: ${response.code}"))
                val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response body"))

                val json = Json { ignoreUnknownKeys = true }
                val searchResponse = json.decodeFromString<ITunesSearchResponse>(body)
                val songList = searchResponse.results.mapNotNull { result ->
                    val title = result.trackName ?: return@mapNotNull null
                    val artist = result.artistName ?: return@mapNotNull null
                    val appleMusicUrl = result.trackViewUrl ?: return@mapNotNull null

                    SongDetails(
                        title = title,
                        artistName = artist,
                        thumbnailUrl = result.artworkUrl100,
                        previewUrl = result.previewUrl?.replace("http://", "https://"),
                        links = PlatformLinks(appleMusic = appleMusicUrl)
                    )
                }
                Result.success(songList)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}