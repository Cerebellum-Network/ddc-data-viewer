package network.cere.ddc.client

import io.smallrye.mutiny.Uni
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.mutiny.core.buffer.Buffer
import io.vertx.mutiny.ext.web.client.HttpResponse
import io.vertx.mutiny.ext.web.client.WebClient
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.zip.CRC32
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class DdcClient(
    private val webClient: WebClient,
    @ConfigProperty(name = "ddc-bootnode") private val bootnode: String
) {
    private companion object {
        private const val API_PREFIX = "/api/rest"
    }

    fun getPieces(getPiecesRequest: GetPiecesRequest): Uni<JsonArray> {
        val crc = CRC32()
        crc.update(getPiecesRequest.userPubKey.toByteArray())
        val ringToken = crc.value

        return webClient.getAbs("$bootnode$API_PREFIX/apps/${getPiecesRequest.appPubKey}/topology")
            .send()
            .map {
                it.bodyAsJsonObject()
                    .getJsonArray("partitions")
                    .map { it as JsonObject }
                    .sortedByDescending { it.getLong("ringToken") }
                    .first { it.getLong("ringToken") <= ringToken }
                    .getJsonObject("master")
                    .getString("nodeHttpAddress")
            }.flatMap { getPiecesFromNode(getPiecesRequest, it) }
    }

    private fun getPiecesFromNode(getPiecesRequest: GetPiecesRequest, node: String): Uni<JsonArray> {
        return webClient.getAbs("$node$API_PREFIX/pieces?appPubKey=${getPiecesRequest.appPubKey}&userPubKey=${getPiecesRequest.userPubKey}")
            .send()
            .map(HttpResponse<Buffer>::bodyAsJsonArray)
    }
}
