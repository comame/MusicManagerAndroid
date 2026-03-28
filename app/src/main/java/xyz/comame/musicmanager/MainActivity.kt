package xyz.comame.musicmanager

import android.app.Activity
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.comame.musicmanager.ui.theme.MusicManagerTheme
import java.lang.Thread.sleep

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AppNotification.createNotificationChannel(getSystemService(NOTIFICATION_SERVICE) as NotificationManager)

        setContent {
            MusicManagerTheme {
                Scaffold() { innerPadding ->
                    Main(
                         modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Main(modifier: Modifier = Modifier) {
    val downloader = remember { MusicManager("http://192.168.0.3:9000") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize()) {
        Button(onClick = { scope.launch { onClick(context, downloader) } }) {
            Text("曲をダウンロードしてみる")
        }
        Button(onClick = { scope.launch { onClick2(context, downloader) } }) {
            Text("ライブラリを保存してみる")
        }
        Button(onClick = { scope.launch { onClick3(context, downloader) } }) {
            Text("保存したライブラリを読み込んでみる")
        }
        Button(onClick = { scope.launch { onClick4(context, downloader) } }) {
            Text("保存した曲の一覧を取得してみる")
        }
        Button(onClick = { onClick5(context) }) {
            Text("ジョブをスケジュールしてみる")
        }
        Button(onClick = { onClick6(context) }) {
            Text("ジョブをキャンセルしてみる")
        }
        Button(onClick = {
            val t = Toast.makeText(context, "Hello, world", Toast.LENGTH_LONG)
            t.show()
        }) {
            Text("トーストを表示してみる")
        }
    }
}

suspend fun onClick(context: Context, downloader: MusicManager) {
    withContext(Dispatchers.IO) {
        val lib = downloader.fetchMusicLibrary()
        Log.d("download", "track count: ${lib.tracks.size}")

        val r = downloader.getMusicTrackDownloadStream(lib.tracks[0].persistentID)
        MusicManager.saveMusicTrackToFile(context, lib.tracks[0].persistentID, r, lib)
    }
}

suspend fun onClick2(context: Context, downloader: MusicManager) {
    withContext(Dispatchers.IO) {
        val library = downloader.fetchMusicLibrary()
        MusicManager.saveLibraryFile(context, library)
    }
}

suspend fun onClick3(context: Context, downloader: MusicManager) {
    withContext(Dispatchers.IO) {
        val library = MusicManager.getLibraryFile(context)
        if (library != null) {
            Log.d("library", "track count: ${library.tracks.size}")
        } else {
            Log.d("library", "no library file")
        }
    }
}

suspend fun onClick4(context: Context, downloader: MusicManager) {
    withContext(Dispatchers.IO) {
        val list = MusicManager.listSavedTrackPersistentIDs(context)
        Log.d("library", "saved tracks: ${list.joinToString(", ")}")
    }
}

fun onClick5(context: Context) {
    val networkRequestBuilder = NetworkRequest.Builder()
        .addCapability(NET_CAPABILITY_INTERNET)
        .build()

    val jobInfo = JobInfo.Builder(1, ComponentName(context, TrackDownloadService::class.java))
        .setUserInitiated(true)
        .setRequiredNetwork(networkRequestBuilder)
        .build()

    val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    scheduler.schedule(jobInfo)
}

fun onClick6(context: Context) {
    val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    scheduler.cancel(1)
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    MusicManagerTheme {
        Main()
    }
}