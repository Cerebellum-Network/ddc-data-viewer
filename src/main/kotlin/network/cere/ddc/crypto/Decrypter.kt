package network.cere.ddc.crypto

import com.google.crypto.tink.Aead
import com.google.crypto.tink.subtle.Hex
import com.google.crypto.tink.subtle.XChaCha20Poly1305
import com.rfksystems.blake2b.Blake2b
import com.rfksystems.blake2b.security.Blake2bProvider
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import java.security.MessageDigest
import java.security.Security
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class Decrypter {
    init {
        Security.addProvider(Blake2bProvider())
    }

    fun decrypt(data: String, masterKey: String) : JsonObject {
        val dataJson = JsonObject(data)
        return dataJson.fieldNames()
            .map { it to masterKey.hex2Bin() + it.toByteArray() }
            .toMap()
            .mapValues { MessageDigest.getInstance(Blake2b.BLAKE2_B_256).apply { update(it.value) }.digest() }
            .mapValues { XChaCha20Poly1305(it.value) }
            .mapValues { sta -> dataJson.getString(sta.key)?.let { decrypt(it, sta.value) } }
            .filterValues { it != null }
            .values
            .fold(JsonObject()) { a, b -> a.mergeIn(b, true) }
    }

    private fun decrypt(data: String, aead: Aead): JsonObject {
        return aead.decrypt(data.hex2Bin(), null)
            .let { Buffer.buffer(it) }
            .let { JsonObject(it) }
    }

    private fun String.hex2Bin(): ByteArray = Hex.decode(this)

    private fun ByteArray.toHex(): String = Hex.encode(this)
}
