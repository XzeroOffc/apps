package com.sonora.musictools

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class MusicTrack(
    val name: String,
    val artist: String,
    val album: String,
    val duration: String,
    val spotifyUrl: String
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
        val response = getJson("$BASE_URL/spotify?q=${Uri.encode(query)}", apiKey)
        val results = response.optJSONArray("result") ?: return@withContext emptyList()
        buildList {
            for (index in 0 until results.length()) {
                val item = results.optJSONObject(index) ?: continue
                val name = item.optString("name").trim()
                if (name.isBlank()) continue
                add(
                    MusicTrack(
                        name = name,
                        artist = item.optString("artist", "Artis tidak diketahui"),
                        album = item.optString("album", "Album tidak diketahui"),
                        duration = item.optString("duration"),
                        spotifyUrl = item.optString("spotifyUrl")
                    )
                )
            }
        }
    }

    suspend fun findLyrics(track: MusicTrack, apiKey: String): TrackLyrics? = withContext(Dispatchers.IO) {
        val query = "${track.name} ${track.artist}"
        val response = getJson("$BASE_URL/lyrics?q=${Uri.encode(query)}", apiKey)
        val results = response.optJSONArray("result") ?: return@withContext null
        var first: TrackLyrics? = null
        for (index in 0 until results.length()) {
            val item = results.optJSONObject(index) ?: continue
            val candidate = TrackLyrics(
                name = item.optString("trackName").ifBlank { item.optString("name", track.name) },
                artist = item.optString("artistName", track.artist),
                album = item.optString("albumName", track.album),
                plainLyrics = item.optString("plainLyrics"),
                syncedLyrics = item.optString("syncedLyrics"),
                instrumental = item.optBoolean("instrumental", false)
            )
            if (first == null) first = candidate
            if (normalize(candidate.name) == normalize(track.name)) return@withContext candidate
        }
        first
    }

    suspend fun getPlayableMedia(track: MusicTrack, apiKey: String): PlayableMedia = withContext(Dispatchers.IO) {
        val query = "${track.name} ${track.artist}"
        val response = getJson("$BASE_URL/play?q=${Uri.encode(query)}", apiKey)
        val media = response.optJSONObject("media")
            ?: throw IOException("Layanan tidak mengembalikan data audio.")
        val audioUrl = media.optString("url").trim()
        if (!audioUrl.startsWith("https://") && !audioUrl.startsWith("http://")) {
            throw IOException("Tautan audio tidak tersedia untuk lagu ini.")
        }
        val videoInfo = response.optJSONObject("video_info")
        PlayableMedia(
            url = audioUrl,
            title = media.optString("title").ifBlank { track.name },
            duration = videoInfo?.optString("duration").orEmpty().ifBlank { track.duration },
            thumbnail = videoInfo?.optString("thumbnail").orEmpty()
        )
    }

    private fun getJson(url: String, apiKey: String): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12_000
            readTimeout = 18_000
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
        } finally {
            connection.disconnect()
        }
    }

    private fun normalize(value: String): String = value
        .lowercase()
        .filter { it.isLetterOrDigit() }
}
