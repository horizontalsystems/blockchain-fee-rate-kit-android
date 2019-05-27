package io.horizontalsystems.feeratekit.api

import android.util.Log
import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import com.eclipsesource.json.JsonValue
import io.reactivex.Maybe
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.util.logging.Logger

class ApiManager {
    private val logger = Logger.getLogger("ApiManager")

    @Throws
    fun getJson(host: String, file: String, timeoutInSeconds: Int): JsonObject {
        return getJsonValue(host, file, timeoutInSeconds).asObject()
    }

    private fun getJsonValue(host: String, file: String, timeoutInSeconds: Int): JsonValue {
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

    //Infura part
    fun getGasPrice(infuraUrl: String): Maybe<BigInteger> {
        val web3j: Web3j = Web3j.build(HttpService(infuraUrl))
        return web3j.ethGasPrice()
            .flowable()
            .map { it.gasPrice }
            .firstElement()
    }

    //BCoin part
    fun getFeeByPriority(priorityInNumberOfBlocks: Int, host: String): Maybe<Float> {
        return Maybe.create { subscriber ->
            try {
                val jsonArray = JsonArray()
                jsonArray.add(priorityInNumberOfBlocks)
                val requestData = JsonObject().apply {
                    this["method"] = "estimatesmartfee"
                    this["params"] = jsonArray
                }

                logger.info("Request feeRate for Bitcoin $requestData")

                val response = post(host, requestData.toString())

                val responseObject = response.asObject()

                val fee = responseObject["result"].asObject()["fee"].asFloat()

                subscriber.onSuccess(fee)
                subscriber.onComplete()
            } catch (e: Exception) {
                Log.e("BCoin", "exception", e)
                subscriber.onError(e)
            }
        }
    }

    private fun post(resource: String, data: String): JsonValue {
        val url = URL(resource)
        val urlConnection = url.openConnection() as HttpURLConnection
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

    companion object{
        //Number of blocks sets priority https://bcoin.io/api-docs/#estimatefee.
        const val bcoinHigh = 1
        const val bcoinMedium = 6
        const val bcoinLow = 15
    }

}
