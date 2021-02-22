package network.cere.ddc.viewer

import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import network.cere.ddc.client.DdcClient
import network.cere.ddc.client.GetPiecesRequest
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Path("/")
class ViewerResource(private val index: Template, private val ddcClient: DdcClient) {
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
}
