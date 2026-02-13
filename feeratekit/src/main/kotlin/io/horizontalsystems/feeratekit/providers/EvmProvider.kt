package io.horizontalsystems.feeratekit.providers

import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import io.horizontalsystems.feeratekit.utils.HttpUtils
import java.math.BigInteger
import java.util.logging.Logger

class EvmProvider(private val url: String, private val torEnabled: Boolean, private val auth: String? = null) {

    private val logger = Logger.getLogger("EvmProvider")

    suspend fun getFeeRate(): BigInteger {
        val requestData = JsonObject().apply {
            this["jsonrpc"] = "2.0"
            this["method"] = "eth_gasPrice"
            this["params"] = JsonArray()
            this["id"] = 1
        }

        val username = auth?.let { "" }
        val response = HttpUtils.post(
            resource = url,
            data = requestData.toString(),
            username = username,
            pswd = auth,
            torEnabled = torEnabled
        )
        val responseObject = response.asObject()

        logger.info("Received gasPrice from Evm $responseObject")

        val gasPriceInHex = responseObject["result"].asString().replace("0x", "")

        return BigInteger(gasPriceInHex, 16)
    }

}
