package xyz.comame.musicmanager

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readAvailable
import java.io.File

class MusicManager(
    private val baseUrl: String,
) {
    // サーバーから MusicLibrary を取得する
    suspend fun fetchMusicLibrary(): MusicLibrary {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
        val res: MusicLibrary = client.get("$baseUrl/library.json").body()

        return res
    }

    // 指定した PersistentID の音楽トラックのダウンロードストリームを取得する
    suspend fun getMusicTrackDownloadStream(persistentID: String): ByteReadChannel {
        val client = HttpClient(CIO)

        val res = client.prepareGet("$baseUrl/track/$persistentID")
        val rcv: ByteReadChannel = res.body()
        return rcv
    }

    companion object {
        // MusicLibrary をストレージに保存する
        suspend fun saveLibraryFile(context: Context, library: MusicLibrary) {
            var dir = context.getExternalFilesDir(null)
            var f = File(dir, "library.json")
            var json =
                kotlinx.serialization.json.Json.encodeToString(MusicLibrary.serializer(), library)
            f.writeText(json)
        }

        // ストレージに保存した MusicLibrary を取得する
        fun getLibraryFile(context: Context): MusicLibrary? {
            val dir = context.getExternalFilesDir(null)
            var f = File(dir, "library.json")
            if (!f.exists()) {
                return null
            }

            var json = f.readText()
            val library =
                kotlinx.serialization.json.Json.decodeFromString(MusicLibrary.serializer(), json)
            return library
        }

        // 音楽トラックを保存する
        suspend fun saveMusicTrackToFile(
            context: Context,
            persistentID: String,
            r: ByteReadChannel,
            library: MusicLibrary
        ) {
            val track = library.tracks.first { it.persistentID == persistentID }

            val resolver = context.contentResolver
            val audioCollection = MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )

            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, track.androidFileName())
                put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/tracks")
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }

            val uri = resolver.insert(audioCollection, values)
            if (uri == null) {
                return;
            }

            try {
                val w = resolver.openOutputStream(uri)
                if (w == null) {
                    return;
                }

                // ストリームを書き込み
                val bufSize = 1 * 1024 * 1024
                val buf = ByteArray(bufSize)
                var i = 0
                while (!r.exhausted()) {
                    val n = r.readAvailable(buf, 0, bufSize)
                    if (n <= 0) {
                        break
                    }
                    i += 1
                    Log.d("download", "written $i times, $n bytes")
                    w.write(buf, 0, n)

                }
                w.flush()

                values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            } catch (err: Exception) {
                resolver.delete(uri, null, null)
            }
        }

        fun listSavedTrackPersistentIDs(context: Context): List<String> {
            val resolver = context.contentResolver
            val audioCollection = MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )

            val projection = arrayOf(
                MediaStore.Audio.Media.DISPLAY_NAME,
            )

            val selection = "${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?"
            val selectionArgs = arrayOf("${Environment.DIRECTORY_MUSIC}/tracks/%")

            val cursor = resolver.query(
                audioCollection,
                projection,
                selection,
                selectionArgs,
                null
            ) ?: return emptyList()

            val persistentIDs = mutableListOf<String>()
            cursor.use {
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val displayName = cursor.getString(nameColumn)
                    val persistentID = displayName.substringBeforeLast(".")
                    persistentIDs.add(persistentID)
                }
            }

            return persistentIDs
        }
    }
}