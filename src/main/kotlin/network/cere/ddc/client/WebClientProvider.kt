package network.cere.ddc.client

import io.vertx.mutiny.core.Vertx
import io.vertx.mutiny.ext.web.client.WebClient
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Produces

@ApplicationScoped
class WebClientProvider(private val vertx: Vertx) {
    @Produces
    fun webClient(): WebClient = WebClient.create(vertx)
}
