package com.sonora.musictools

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

data class MusicTrack(
    val title: String,
    val artist: String,
    val videoId: String,
    val videoUrl: String,
    val thumbnail: String,
    val duration: String,
    val uploaded: String,
    val views: String
)

data class TrackLyrics(
    val name: String,
    val artist: String,
    val album: String,
    val plainLyrics: String,
    val syncedLyrics: String,
    val instrumental: Boolean
)

data class PlayableMedia(
    val url: String,
    val title: String,
    val duration: String,
    val thumbnail: String
)

object MusicApi {
    private const val BASE_URL = "https://sylvatica.my.id/api/search"

    suspend fun searchTracks(query: String, apiKey: String): List<MusicTrack> = withContext(Dispatchers.IO) {
        val response = getJson("$BASE_URL/yts?q=${Uri.encode(query)}", apiKey)
        val results = response.optJSONArray("result") ?: return@withContext emptyList()
        buildList {
            for (index in 0 until results.length()) {
                val item = results.optJSONObject(index) ?: continue
                val rawTitle = item.optString("title").trim()
                if (rawTitle.isBlank()) continue
                val cleanedTitle = cleanYouTubeTitle(rawTitle)
                val titleParts = cleanedTitle.split(" - ", limit = 2)
                add(
                    MusicTrack(
                        title = titleParts.last().trim().ifBlank { cleanedTitle },
                        artist = if (titleParts.size > 1) titleParts.first().trim() else "",
                        videoId = item.optString("videoId"),
                        videoUrl = item.optString("url"),
                        thumbnail = item.optString("thumbnail"),
                        duration = item.optString("duration"),
                        uploaded = item.optString("uploaded"),
                        views = item.optString("views")
                    )
                )
            }
        }
    }

    suspend fun findLyrics(track: MusicTrack, apiKey: String): TrackLyrics? = withContext(Dispatchers.IO) {
        var first: TrackLyrics? = null
        val queries = listOf("${track.artist} ${track.title}".trim(), track.title).distinct()
        for (query in queries) {
            val response = getJson("$BASE_URL/lyrics?q=${Uri.encode(query)}", apiKey)
            val results = response.optJSONArray("result") ?: continue
            for (index in 0 until results.length()) {
                val item = results.optJSONObject(index) ?: continue
                val candidate = TrackLyrics(
                    name = item.optString("trackName").ifBlank { item.optString("name", track.title) },
                    artist = item.optString("artistName", track.artist),
                    album = item.optString("albumName"),
                    plainLyrics = item.optString("plainLyrics"),
                    syncedLyrics = item.optString("syncedLyrics"),
                    instrumental = item.optBoolean("instrumental", false)
                )
                if (first == null) first = candidate
                val candidateTitle = normalize(candidate.name)
                val selectedTitle = normalize(track.title)
                if (candidateTitle == selectedTitle || selectedTitle.contains(candidateTitle) || candidateTitle.contains(selectedTitle)) {
                    return@withContext candidate
                }
            }
        }
        first
    }

    suspend fun getPlayableMedia(track: MusicTrack, apiKey: String): PlayableMedia = withContext(Dispatchers.IO) {
        val query = listOf(track.artist, track.title).filter { it.isNotBlank() }.joinToString(" ")
        val response = getJson("$BASE_URL/play?q=${Uri.encode(query)}", apiKey, readTimeoutMs = 120_000)
        val media = response.optJSONObject("media")
            ?: throw IOException("Layanan tidak mengembalikan data audio.")
        val audioUrl = media.optString("url").trim()
        if (!audioUrl.startsWith("https://") && !audioUrl.startsWith("http://")) {
            throw IOException("Tautan audio tidak tersedia untuk lagu ini.")
        }
        val videoInfo = response.optJSONObject("video_info")
        PlayableMedia(
            url = audioUrl,
            title = media.optString("title").ifBlank { track.title },
            duration = videoInfo?.optString("duration").orEmpty().ifBlank { track.duration },
            thumbnail = videoInfo?.optString("thumbnail").orEmpty().ifBlank { track.thumbnail }
        )
    }

    private fun getJson(url: String, apiKey: String, readTimeoutMs: Int = 18_000): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12_000
            readTimeout = readTimeoutMs
            setRequestProperty("Accept", "application/json")
            setRequestProperty("X-API-Key", apiKey)
        }
        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(body) }.getOrElse {
                throw IOException("Respons API tidak dapat dibaca.")
            }
            if (status !in 200..299 || !json.optBoolean("status", true)) {
                throw IOException(json.optString("message").ifBlank { "Permintaan ke layanan musik gagal ($status)." })
            }
            return json
        } catch (exception: SocketTimeoutException) {
            val message = if (url.contains("/play?")) {
                "Layanan audio masih memproses permintaan. Tunggu sebentar, lalu coba lagi."
            } else {
                "Permintaan melewati batas waktu. Periksa koneksi lalu coba lagi."
            }
            throw IOException(message, exception)
        } finally {
            connection.disconnect()
        }
    }

    private fun normalize(value: String): String = value
        .lowercase()
        .filter { it.isLetterOrDigit() }

    private fun cleanYouTubeTitle(value: String): String = value
        .replace(Regex("\\s*[\\[(].*?(official|lyric|lyrics|lirik|video|audio|visualizer).*?[\\])]+\\s*", RegexOption.IGNORE_CASE), "")
        .replace(Regex("\\s*[-|]\\s*(official\\s+)?(music\\s+)?(lyric|lyrics|lirik|audio)?\\s*video.*$", RegexOption.IGNORE_CASE), "")
        .trim()
}
