package network.cere.ddc.viewer

import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import io.vertx.core.json.JsonObject
import network.cere.ddc.client.DdcClient
import network.cere.ddc.client.GetPiecesRequest
import network.cere.ddc.crypto.Decrypter
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/")
class ViewerResource(
    private val index: Template,
    private val ddcClient: DdcClient,
    private val decrypter: Decrypter
) {
    @GET
    @Produces(MediaType.TEXT_HTML)
    fun index(): TemplateInstance = index.instance()

    @GET
    @Path("/data")
    @Produces(MediaType.TEXT_PLAIN)
    fun getData(
        @QueryParam("appPubKey") appPubKey: String,
        @QueryParam("userPubKey") userPubKey: String
    ) = ddcClient.getPieces(GetPiecesRequest(appPubKey, userPubKey))

    @POST
    @Path("/decrypt")
    @Produces(MediaType.TEXT_PLAIN)
    fun decrypt(data: String): String {
        val rq = JsonObject(data)
        return decrypter.decrypt(rq.getString("data"), rq.getString("key")).encodePrettily()
    }
}
