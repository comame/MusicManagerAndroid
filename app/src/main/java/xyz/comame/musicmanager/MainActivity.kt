package xyz.comame.musicmanager

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.comame.musicmanager.ui.theme.MusicManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicManagerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val downloader = remember { MusicDownloader("http://192.168.0.3:9000") }
    val scope = rememberCoroutineScope()

    Box(contentAlignment = Alignment.Center) {
        Button(onClick = { scope.launch { onClick(downloader) } }) {
            Text("Music Library をダウンロード")
        }
    }
}

suspend fun onClick(downloader: MusicDownloader) {
    withContext(Dispatchers.IO) {
        val lib = downloader.fetchMusicLibrary()
        Log.d("dbg", "size: ${lib.tracks.size}")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MusicManagerTheme {
        Greeting("Android")
    }
}