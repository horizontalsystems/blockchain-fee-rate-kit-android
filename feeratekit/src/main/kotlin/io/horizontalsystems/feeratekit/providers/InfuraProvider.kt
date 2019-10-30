package io.horizontalsystems.feeratekit.providers

import android.util.Base64
import android.util.Log
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import io.horizontalsystems.feeratekit.model.Coin
import io.horizontalsystems.feeratekit.model.FeeRate
import io.horizontalsystems.feeratekit.utils.HttpUtils
import io.reactivex.Maybe
import java.math.BigInteger
import java.util.*
import java.util.logging.Logger

class InfuraProvider(
    val infuraProjectId: String? = null,
    val infuraProjectSecret: String? = null,
    var infuraApiUrl: String = "https://mainnet.infura.io/v3/") {

    private val logger = Logger.getLogger("InfuraProvider")

    private fun getGasPrice(): Maybe<BigInteger> {
        return Maybe.create { subscriber ->
            try {
                val requestData = JsonObject().apply {
                    this["jsonrpc"] = "2.0"
                    this["method"] = "eth_gasPrice"
                    this["params"] = JsonArray()
                    this["id"] = 1
                }

                val url = "$infuraApiUrl$infuraProjectId"

                val userCredentials = ":$infuraProjectSecret"
                val basicAuth = "Basic " + String(Base64.encode(userCredentials.toByteArray(), Base64.DEFAULT))

                val response = HttpUtils.post(url, requestData.toString(), basicAuth)
                val responseObject = response.asObject()

                logger.info("Received gasPrice fromInfura $responseObject")

                val gasPriceInHex = responseObject["result"].asString().replace("0x", "")

                val gasPrice = BigInteger(gasPriceInHex, 16)

                subscriber.onSuccess(gasPrice)
                subscriber.onComplete()
            } catch (e: Exception) {
                Log.e("Infura", "exception", e)
                subscriber.onError(e)
            }
        }
    }

    fun getFeeRates(): Maybe<FeeRate> {

        if (infuraProjectId == null || infuraProjectSecret == null)
            return Maybe.empty()

        return getGasPrice()
            .flatMap { mediumBigInt ->
                val medium = mediumBigInt.toLong()
                val low: Long = medium / 2.toLong()
                val high: Long = medium * 2

                val coin = Coin.ETHEREUM
                val defaultRate = coin.defaultRate()

                return@flatMap Maybe.just(
                    FeeRate(
                        coin = coin,
                        date = Date().time,
                        lowPriority = low,
                        lowPriorityDuration = defaultRate.lowPriorityDuration,
                        mediumPriority = medium,
                        mediumPriorityDuration = defaultRate.mediumPriorityDuration,
                        highPriority = high,
                        highPriorityDuration = defaultRate.highPriorityDuration
                    )
                )
            }
    }
}