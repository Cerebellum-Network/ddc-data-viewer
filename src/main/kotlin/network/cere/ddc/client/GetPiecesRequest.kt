package network.cere.ddc.client

data class GetPiecesRequest(
    val appPubKey: String,
    val userPubKey: String
)
