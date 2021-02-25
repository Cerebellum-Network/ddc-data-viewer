package network.cere.ddc.viewer

import com.google.crypto.tink.subtle.Ed25519Sign
import com.google.crypto.tink.subtle.Hex
import io.quarkus.test.junit.QuarkusTest
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.When
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.mutiny.ext.web.client.WebClient
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.zip.CRC32
import javax.inject.Inject

@QuarkusTest
internal class ViewerResourceTest {
    private companion object {
        private const val API_PREFIX = "/api/rest"
    }

    @ConfigProperty(name = "ddc-bootnode")
    private lateinit var bootnode: String

    @Inject
    private lateinit var webClient: WebClient

    @Test
    fun `Get data`() {
        // 1. Create app
        val appPubKey = "0x8f01969eb5244d853cc9c6ad73c46d8a1a091842c414cabd2377531f0832635f"
        val appPrivKey = Hex.decode("38a538d3d890bfe8f76dc9bf578e215af16fd3d684666f72db0bc0a22bc1d05b")
        val tierId = "1"
        val appSignature = Ed25519Sign(appPrivKey)
            .sign("$appPubKey$tierId".toByteArray())
            .let(Hex::encode)

        webClient.postAbs("$bootnode$API_PREFIX/apps")
            .sendJsonObjectAndAwait(
                JsonObject(
                    """
                        {
                            "appPubKey": "$appPubKey",
                            "tierId": "$tierId",
                            "signature": "$appSignature"
                        }
                    """.trimIndent()
                )
            )

        await atMost Duration.ofMinutes(2) until {
            webClient.getAbs("$bootnode$API_PREFIX/apps/$appPubKey").sendAndAwait().statusCode() == 200
        }

        // 2. Submit data
        val signer = Ed25519Sign(appPrivKey)
        val id = UUID.randomUUID().toString()
        val timestamp = Instant.now().toString()
        val userPubKey = "0x1"
        val data = "{}"
        val signature = signer.sign("$id$timestamp$appPubKey$userPubKey$data".toByteArray()).let(Hex::encode)
        val piece = JsonObject(
            """
                    {
                      "id": "$id",
                      "appPubKey": "$appPubKey",
                      "userPubKey": "$userPubKey",
                      "timestamp": "$timestamp",
                      "data": "$data",
                      "signature": "$signature"
                    }
                """.trimIndent()
        )
        val targetNode = searchTargetPartitionNode(piece)
        webClient.postAbs("$targetNode$API_PREFIX/pieces").sendJsonObjectAndAwait(piece)
            .also { assertEquals(201, it.statusCode()) }

        // 3. Get data
        When {
            get("/data?appPubKey=$appPubKey&userPubKey=$userPubKey")
        } Extract {
            val rs = JsonArray(body().asString())
            assertEquals(JsonArray(listOf(piece.apply { remove("signature") })), rs)
        }
    }

    private fun searchTargetPartitionNode(piece: JsonObject): String {
        val crc = CRC32()
        crc.update(piece.getString("userPubKey").toByteArray())
        val ringToken = crc.value
        return webClient.getAbs("$bootnode$API_PREFIX/apps/${piece.getString("appPubKey")}/topology")
            .sendAndAwait()
            .bodyAsJsonObject()
            .getJsonArray("partitions")
            .map { it as JsonObject }
            .sortedByDescending { it.getLong("ringToken") }
            .first { it.getLong("ringToken") <= ringToken }
            .getJsonObject("master")
            .getString("nodeHttpAddress")
    }
}
