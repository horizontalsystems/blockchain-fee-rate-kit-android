package io.horizontalsystems.feeratekit.providers

import io.horizontalsystems.feeratekit.model.FeeRate
import io.reactivex.Single


interface IFeeRateProvider {
    fun getFeeRates(): Single<FeeRate>
}