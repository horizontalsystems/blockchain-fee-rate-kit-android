package io.horizontalsystems.feeratekit.api

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.eclipsesource.json.JsonValue
import java.net.URL
import java.util.logging.Logger

class ApiManager(private val host: String) {
    private val logger = Logger.getLogger("ApiManager")

    @Throws
    fun getJson(file: String, timeoutInSeconds: Int): JsonObject {
        return getJsonValue(file, timeoutInSeconds).asObject()
    }

    private fun getJsonValue(file: String, timeoutInSeconds: Int): JsonValue {
        val resource = "$host/$file"

        logger.info("Fetching $resource")

        return URL(resource)
            .openConnection()
            .apply {
                connectTimeout = 5000
                readTimeout = timeoutInSeconds * 1000
                setRequestProperty("Accept", "application/json")
            }
            .getInputStream()
            .use {
                Json.parse(it.bufferedReader())
            }
    }

}
