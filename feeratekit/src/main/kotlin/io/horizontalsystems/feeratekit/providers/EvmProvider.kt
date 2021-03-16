package io.horizontalsystems.feeratekit.providers

import android.util.Base64
import android.util.Log
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import io.horizontalsystems.feeratekit.utils.HttpUtils
import io.reactivex.Single
import java.math.BigInteger
import java.util.logging.Logger

class EvmProvider(private val url: String, private val auth: String? = null) {

    private val logger = Logger.getLogger("EvmProvider")

    fun getFeeRate(): Single<BigInteger> {
        return Single.create { subscriber ->
            try {

                val requestData = JsonObject().apply {
                    this["jsonrpc"] = "2.0"
                    this["method"] = "eth_gasPrice"
                    this["params"] = JsonArray()
                    this["id"] = 1
                }

                val username = auth?.let { "" }
                val response = HttpUtils.post(url, requestData.toString(), username, auth)
                val responseObject = response.asObject()

                logger.info("Received gasPrice from Evm $responseObject")

                val gasPriceInHex = responseObject["result"].asString().replace("0x", "")

                val gasPrice = BigInteger(gasPriceInHex, 16)

                subscriber.onSuccess(gasPrice)

            } catch (e: Exception) {
                Log.e("EvmProvider", "exception", e)
                subscriber.onError(e)
            }
        }
    }

}
