package network.cere.ddc.crypto

import com.google.crypto.tink.Aead
import com.google.crypto.tink.subtle.Hex
import com.google.crypto.tink.subtle.XChaCha20Poly1305
import com.rfksystems.blake2b.security.Blake2b256Digest
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class Decrypter {
    fun decrypt(data: String, masterKey: String) : JsonObject {
        val dataJson = JsonObject(data)
        return dataJson.fieldNames()
            .map { it to masterKey.hex2Bin() + it.toByteArray() }
            .toMap()
            .mapValues { Blake2b256Digest().apply { update(it.value) }.digest() }
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
