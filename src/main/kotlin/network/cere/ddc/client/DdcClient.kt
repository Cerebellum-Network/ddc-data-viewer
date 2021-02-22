package network.cere.ddc.client

import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.mutiny.ext.web.client.WebClient
import org.eclipse.microprofile.config.inject.ConfigProperty
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
        return webClient.getAbs("$bootnode$API_PREFIX/apps/${getPiecesRequest.appPubKey}/topology")
            .send()
            .map {
                if (it.statusCode() != 200) {
                    emptyList()
                } else {
                    it.bodyAsJsonObject()
                        .getJsonArray("partitions")
                        .asSequence()
                        .map { it as JsonObject }
                        .map { it.getJsonObject("master") }
                        .map { it.getString("nodeHttpAddress") }
                        .toList()
                }
            }.onItem()
            .transformToMulti { Multi.createFrom().iterable(it) }
            .onItem()
            .transformToUniAndMerge { getPiecesFromNode(getPiecesRequest, it) }
            .filter { !it.isEmpty }
            .transform()
            .byTakingFirstItems(1)
            .toUni()
    }

    private fun getPiecesFromNode(getPiecesRequest: GetPiecesRequest, node: String): Uni<JsonArray> {
        return webClient.getAbs("$node$API_PREFIX/pieces?appPubKey=${getPiecesRequest.appPubKey}&userPubKey=${getPiecesRequest.userPubKey}")
            .send()
            .onItemOrFailure()
            .transform { rs, throwable ->
                if (throwable != null || rs.statusCode() != 200) JsonArray() else rs.bodyAsJsonArray()
            }
    }
}
