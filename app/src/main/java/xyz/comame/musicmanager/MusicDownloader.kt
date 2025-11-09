package xyz.comame.musicmanager

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json

class MusicDownloader(
    private val baseUrl: String,
) {
    suspend fun fetchMusicLibrary(): MusicLibrary {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
        val res: MusicLibrary = client.get("$baseUrl/library.json").body()
        return res
    }
}