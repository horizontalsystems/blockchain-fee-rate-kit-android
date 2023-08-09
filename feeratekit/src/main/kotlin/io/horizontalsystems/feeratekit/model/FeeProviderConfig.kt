package io.horizontalsystems.feeratekit.model

data class FeeProviderConfig(
    val ethEvmUrl: String,
    val ethEvmAuth: String? = null,
    val bscEvmUrl: String,
    val mempoolSpaceUrl: String,
){

    companion object{
        fun defaultBscEvmUrl(): String = "https://bsc-dataseed.binance.org"

        fun infuraUrl(projectId: String): String {
            return "https://mainnet.infura.io/v3/$projectId"
        }
    }
}
