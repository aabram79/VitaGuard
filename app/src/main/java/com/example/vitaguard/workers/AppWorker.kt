package com.example.vitaguard.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.vitaguard.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val variablePart = "variable content"
        val postMessage = "This is a post message with $variablePart"
        val url = "http://141.148.65.60:3000"

        val result = NetworkUtils.sendVolleyPostRequest(applicationContext, url, postMessage)

        if (result) {
            Log.d("PingWorker", "Ping sent successfully")
            Result.success()
        } else {
            Log.e("PingWorker", "Ping failed")
            Result.retry()
        }
    }
}
