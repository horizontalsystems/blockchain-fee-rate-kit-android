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

        private val httpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

        fun post(resource: String, data: String, username: String?, pswd: String?): JsonValue {
            val requestBuilder = Request.Builder()
            if (username != null && pswd != null) {
                requestBuilder.addHeader("Authorization", Credentials.basic(username, pswd))
            }

            val request = requestBuilder
                .url(resource)
                .post(data.toRequestBody("application/json".toMediaType()))
                .build()

            val responseString = httpClient.newCall(request).execute().body!!.string()
            Log.e("AAA", "resource: $resource\ndata:$data\nresponse:$responseString")
            return Json.parse(responseString)
        }
    }
}