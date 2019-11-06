package io.horizontalsystems.feeratekit.utils

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonValue
import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class HttpUtils {

    companion object {

        private const val CONNECTION_TIMEOUT = 5 * 1000 // 5 seconds

        fun post(resource: String, data: String, basicAuth: String? = null): JsonValue {

            val url = URL(resource)
            val urlConnection = url.openConnection() as HttpURLConnection
            basicAuth?.let {
                urlConnection.setRequestProperty("Authorization", it)
            }
            urlConnection.requestMethod = "POST"
            urlConnection.doOutput = true
            urlConnection.connectTimeout = CONNECTION_TIMEOUT
            urlConnection.readTimeout = CONNECTION_TIMEOUT

            val out = BufferedOutputStream(urlConnection.outputStream)
            val writer = BufferedWriter(OutputStreamWriter(out, "UTF-8"))

            writer.write(data)
            writer.flush()
            writer.close()
            out.close()

            return urlConnection.inputStream.use {
                Json.parse(it.bufferedReader())
            }
        }
    }
}