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

        fun post(resource: String, data: String, basicAuth: String? = null): JsonValue {

            val url = URL(resource)
            val urlConnection = url.openConnection() as HttpURLConnection

            basicAuth?.let {
                urlConnection.setRequestProperty("Authorization", it)
            }
            urlConnection.requestMethod = "POST"
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