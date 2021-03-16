package io.horizontalsystems.feeratekit.providers

import android.util.Base64
import android.util.Log
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import io.horizontalsystems.feeratekit.model.FeeProviderConfig
import io.horizontalsystems.feeratekit.utils.HttpUtils
import io.reactivex.Single
import java.math.BigInteger
import java.util.logging.Logger

/* Smart Fee RPC Response
         {
            "error": null,
            "id": "curltest",
            "result": {
                "blocks": 10,
                "feerate": 0.00019212
            }
        }

    private val LOW_PRIORITY_BLOCKS = 100
    private val MEDIUM_PRIORITY_BLOCKS = 10
    private val HIGH_PRIORITY_BLOCKS = 1
    */

/**
 * Bitcoin-Core RPC Fee provider
 */
class BtcCoreProvider(private val providerConfig: FeeProviderConfig) {

    private val logger = Logger.getLogger("BtcCoreProvider")

    private fun getEstimatedSmartFee(priorityInNumberOfBlocks: Int): Single<Float> {
        return Single.create { subscriber ->
            try {
                val btcCoreRpcUrl = providerConfig.btcCoreRpcUrl ?: throw Exception("Rpc Url is not provided")

                val jsonArray = JsonArray()

                jsonArray.add(priorityInNumberOfBlocks)

                val requestData = JsonObject().apply {
                    this["jsonrpc"] = "2.0"
                    this["method"] = "estimatesmartfee"
                    this["params"] = jsonArray
                    this["id"] = 1
                }

                logger.info("Request feeRate for Bitcoin $requestData")

                val response = HttpUtils.post(btcCoreRpcUrl, requestData.toString(), providerConfig.btcCoreRpcUser, providerConfig.btcCoreRpcPassword)
                val responseObject = response.asObject()

                val fee = if (responseObject["result"].asObject()["feerate"] != null)
                    responseObject["result"].asObject()["feerate"].asFloat()
                else
                    responseObject["result"].asObject()["fee"].asFloat()

                subscriber.onSuccess(fee)

            } catch (e: Exception) {
                Log.e("Bitcoin-Core", "exception", e)
                subscriber.onError(e)
            }
        }
    }

    fun getFeeRate(blocksCount: Int): Single<BigInteger> {
        return getEstimatedSmartFee(blocksCount).map {
            BigInteger.valueOf(feeInSatoshiPerByte(it))
        }
    }

    private fun feeInSatoshiPerByte(feePerKbyte: Float): Long {
        (feePerKbyte * 100_000_000 / 1024).toLong().let {
            return if (it < 1) 1 else it
        }
    }
}
