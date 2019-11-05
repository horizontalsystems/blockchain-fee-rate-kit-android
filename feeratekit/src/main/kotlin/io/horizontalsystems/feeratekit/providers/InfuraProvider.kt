package io.horizontalsystems.feeratekit.providers

import android.util.Base64
import android.util.Log
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import io.horizontalsystems.feeratekit.model.Coin
import io.horizontalsystems.feeratekit.model.FeeProviderConfig
import io.horizontalsystems.feeratekit.model.FeeRate
import io.horizontalsystems.feeratekit.utils.HttpUtils
import io.reactivex.Single
import java.math.BigInteger
import java.util.*
import java.util.logging.Logger

class InfuraProvider(private val providerConfig: FeeProviderConfig) : IFeeRateProvider {

    private val logger = Logger.getLogger("InfuraProvider")

    private fun getGasPrice(): Single<BigInteger> {
        return Single.create { subscriber ->
            try {

                var infuraUrl: String = "https://mainnet.infura.io/v3/"

                if (!providerConfig.infuraApiUrl.isNullOrEmpty())
                    infuraUrl = providerConfig.infuraApiUrl

                val requestData = JsonObject().apply {
                    this["jsonrpc"] = "2.0"
                    this["method"] = "eth_gasPrice"
                    this["params"] = JsonArray()
                    this["id"] = 1
                }

                val url = "${infuraUrl}${providerConfig.infuraProjectId}"

                val userCredentials = ":${providerConfig.infuraProjectSecret}"
                val basicAuth = "Basic " + String(Base64.encode(userCredentials.toByteArray(), Base64.DEFAULT))

                val response = HttpUtils.post(url, requestData.toString(), basicAuth)
                val responseObject = response.asObject()

                logger.info("Received gasPrice fromInfura $responseObject")

                val gasPriceInHex = responseObject["result"].asString().replace("0x", "")

                val gasPrice = BigInteger(gasPriceInHex, 16)

                subscriber.onSuccess(gasPrice)

            } catch (e: Exception) {
                Log.e("Infura", "exception", e)
                subscriber.onError(e)
            }
        }
    }

    override fun getFeeRates(): Single<FeeRate> {

        return getGasPrice()
                .flatMap { mediumBigInt ->
                    val medium = mediumBigInt.toLong()
                    val low: Long = medium / 2.toLong()
                    val high: Long = medium * 2

                    val coin = Coin.ETHEREUM
                    val defaultRate = coin.defaultRate()

                    return@flatMap Single.just(
                            FeeRate(
                                    coin = coin,
                                    lowPriority = low,
                                    lowPriorityDuration = defaultRate.lowPriorityDuration,
                                    mediumPriority = medium,
                                    mediumPriorityDuration = defaultRate.mediumPriorityDuration,
                                    highPriority = high,
                                    highPriorityDuration = defaultRate.highPriorityDuration,
                                    date = Date().time / 1000
                            )
                    )
                }
    }
}