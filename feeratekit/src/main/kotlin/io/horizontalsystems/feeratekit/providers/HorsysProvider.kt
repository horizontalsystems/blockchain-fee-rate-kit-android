package io.horizontalsystems.feeratekit.providers

import io.horizontalsystems.feeratekit.model.Coin
import io.horizontalsystems.feeratekit.model.FeeRate
import io.horizontalsystems.feeratekit.utils.HttpUtils
import io.reactivex.Single
import io.reactivex.functions.Function3
import java.util.*
import java.util.logging.Logger

/* Smart Fee Response
         {
            "error": null,
            "id": "curltest",
            "result": {
                "blocks": 10,
                "feerate": 0.00019212
            }
        }
    */

/**
 * Bitcoin-Horsys Fee provider
 */
class HorsysProvider(private val coin: Coin) : IFeeRateProvider {

    private val logger = Logger.getLogger("HorsysProvider")
    private val baseUrl = "https://${coin.code.toLowerCase(Locale.ENGLISH)}.horizontalsystems.xyz"

    private val LOW_PRIORITY_BLOCKS = 100
    private val MEDIUM_PRIORITY_BLOCKS = 10
    private val HIGH_PRIORITY_BLOCKS = 1

    private fun getEstimatedSmartFee(priorityInNumberOfBlocks: Int): Single<Float> {
        return Single.create { subscriber ->
            try {
                val apiMethod = when (priorityInNumberOfBlocks) {
                    LOW_PRIORITY_BLOCKS -> "services/fee/low"
                    MEDIUM_PRIORITY_BLOCKS -> "services/fee/avg"
                    HIGH_PRIORITY_BLOCKS -> "services/fee/high"
                    else -> null
                }

                apiMethod?.let {
                    val response = HttpUtils.post("$baseUrl/$apiMethod", "", null)
                    val responseObject = response.asObject()
                    logger.info("Response feeRate for ${coin.code} $response")
                    val fee = responseObject["result"].asObject()["feerate"].asFloat()

                    subscriber.onSuccess(fee)
                }
            } catch (e: Exception) {
                subscriber.onError(e)
            }
        }
    }

    override fun getFeeRates(): Single<FeeRate> {

        return Single.zip(getEstimatedSmartFee(LOW_PRIORITY_BLOCKS),
            getEstimatedSmartFee(MEDIUM_PRIORITY_BLOCKS),
            getEstimatedSmartFee(HIGH_PRIORITY_BLOCKS),
            Function3<Float, Float, Float, Triple<Float, Float, Float>> { t1, t2, t3 ->
                Triple(
                    t1,
                    t2,
                    t3
                )
            })
            .map {
                val defaultRate = coin.defaultRate()
                FeeRate(
                    coin = coin,
                    lowPriority = feeInSatoshiPerByte(it.first),
                    lowPriorityDuration = defaultRate.lowPriorityDuration,
                    mediumPriority = feeInSatoshiPerByte(it.second),
                    mediumPriorityDuration = defaultRate.mediumPriorityDuration,
                    highPriority = feeInSatoshiPerByte(it.third),
                    highPriorityDuration = defaultRate.highPriorityDuration,
                    date = Date().time / 1000
                )
            }
    }

    private fun feeInSatoshiPerByte(feePerKbyte: Float): Long {
        (feePerKbyte * 100_000_000 / 1024).toLong().let {
            return if (it < 1) 1 else it
        }
    }
}
