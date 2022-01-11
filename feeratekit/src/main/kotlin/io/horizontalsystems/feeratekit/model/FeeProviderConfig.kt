package io.horizontalsystems.feeratekit.model

class FeeProviderConfig(
    val ethEvmUrl: String,
    val ethEvmAuth: String? = null,
    val bscEvmUrl: String,
    val btcCoreRpcUrl: String? = null,
    val btcCoreRpcUser: String? = null,
    val btcCoreRpcPassword: String? = null){

    companion object{
        fun defaultBscEvmUrl(): String = "https://bsc-dataseed.binance.org"

        fun infuraUrl(projectId: String): String {
            return "https://mainnet.infura.io/v3/$projectId"
        }
    }
}
