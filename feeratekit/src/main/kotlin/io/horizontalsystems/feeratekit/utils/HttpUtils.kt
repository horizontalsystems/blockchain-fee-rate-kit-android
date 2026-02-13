package io.horizontalsystems.feeratekit.utils

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
            val client = if (torEnabled) torHttpClient else httpClient

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
        }

        fun get(
            resource: String,
            torEnabled: Boolean = false
        ): JsonValue {
            val client = if (torEnabled) torHttpClient else httpClient

            val request = Request.Builder()
                .url(resource)
                .get()
                .build()

            val response = client.newCall(request).execute()

            return Json.parse(response.body!!.charStream())
        }
    }
}