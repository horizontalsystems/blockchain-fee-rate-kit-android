package io.horizontalsystems.feeratekit.utils

import android.util.Log
import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonValue
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class HttpUtils {

    companion object {

        private const val TAG = "HttpUtils"
        
        private val httpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
        
        private val torHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .build()

        fun post(
            resource: String,
            data: String,
            username: String?,
            pswd: String?,
            torEnabled: Boolean = false
        ): JsonValue {
            val maxRetries = if (torEnabled) 3 else 1

            var lastException: Exception? = null

            repeat(maxRetries) { attempt ->
                try {
                    val client = if (torEnabled) {
                        // Increase timeout with each attempt: 20s, 60s, 100s
                        val timeout = 20L + (attempt * 40L)
                        torHttpClient.newBuilder()
                            .connectTimeout(timeout, TimeUnit.SECONDS)
                            .readTimeout(timeout, TimeUnit.SECONDS)
                            .callTimeout(timeout + 30, TimeUnit.SECONDS)
                            .build()
                    } else {
                        httpClient
                    }

                    val requestBuilder = Request.Builder()
                    if (username != null && pswd != null) {
                        requestBuilder.addHeader("Authorization", Credentials.basic(username, pswd))
                    }

                    val request = requestBuilder
                        .url(resource)
                        .post(data.toRequestBody("application/json".toMediaType()))
                        .build()

                    val response = client.newCall(request).execute()

                    return Json.parse(response.body!!.charStream())
                } catch (e: Exception) {
                    lastException = e
                    Log.e(TAG, "Attempt ${attempt + 1}/$maxRetries failed: ${e.javaClass.simpleName} - ${e.message}")

                    if (attempt < maxRetries - 1 && torEnabled) {
                        val sleepTime = 1000L * (attempt + 1)
                        Log.e(TAG, "Retrying in ${sleepTime}ms...")
                        Thread.sleep(sleepTime)
                    }
                }
            }

            throw IllegalStateException(
                "Failed to connect to $resource after $maxRetries attempts (Tor/proxy may be slow or unreachable)",
                lastException
            )
        }

        fun get(
            resource: String,
            torEnabled: Boolean = false
        ): JsonValue {
            val maxRetries = if (torEnabled) 3 else 1

            var lastException: Exception? = null

            repeat(maxRetries) { attempt ->
                try {
                    val client = if (torEnabled) {
                        // Increase timeout with each attempt: 20s, 60s, 100s
                        val timeout = 20L + (attempt * 40L)
                        torHttpClient.newBuilder()
                            .connectTimeout(timeout, TimeUnit.SECONDS)
                            .readTimeout(timeout, TimeUnit.SECONDS)
                            .callTimeout(timeout + 30, TimeUnit.SECONDS)
                            .build()
                    } else {
                        httpClient
                    }

                    val requestBuilder = Request.Builder()

                    val request = requestBuilder
                        .url(resource)
                        .get()
                        .build()

                    val response = client.newCall(request).execute()

                    return Json.parse(response.body!!.charStream())
                } catch (e: Exception) {
                    lastException = e
                    Log.e(TAG, "Attempt ${attempt + 1}/$maxRetries failed: ${e.javaClass.simpleName} - ${e.message}")

                    if (attempt < maxRetries - 1 && torEnabled) {
                        val sleepTime = 1000L * (attempt + 1)
                        Log.e(TAG, "Retrying in ${sleepTime}ms...")
                        Thread.sleep(sleepTime)
                    }
                }
            }

            throw IllegalStateException(
                "Failed to connect to $resource after $maxRetries attempts (Tor/proxy may be slow or unreachable)",
                lastException
            )
        }
    }
}