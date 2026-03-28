package xyz.comame.musicmanager

import android.app.Notification
import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Thread.sleep

class TrackDownloadService : JobService() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onStartJob(params: JobParameters): Boolean {
        showNotification(params, "ダウンロード中")

        val library = MusicManager.getLibraryFile(applicationContext)
        Log.d("download", "library count ${library?.tracks?.size ?: 0}")

        scope.launch {
            Log.d("download", "launched ${params.jobId}")
            try {
                for (i in 1..10) {
                    delay(1000)
                    Log.d("download", "downloading ${params.jobId} $i")
                    showNotification(params, "ダウンロード中 $i")
                }
                jobFinished(params, false)
            } finally {
                Log.d("download", "finished ${params.jobId}")
            }
        }

        Log.d("download", "returned ${params.jobId}")
        return true;
    }

    override fun onStopJob(params: JobParameters): Boolean {
        Log.d("download", "stopped ${params.jobId}")
        scope.cancel()
        return true
    }

    fun showNotification(params: JobParameters, content: String) {
        val notification = Notification.Builder(applicationContext, AppNotification.CHANNEL_ID)
            .setContentTitle(content)
            .setSmallIcon(android.R.mipmap.sym_def_app_icon)
            .build()

        setNotification(params, 53, notification, JobService.JOB_END_NOTIFICATION_POLICY_REMOVE)
    }
}