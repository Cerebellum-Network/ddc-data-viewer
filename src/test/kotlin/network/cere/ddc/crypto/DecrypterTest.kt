package network.cere.ddc.crypto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DecrypterTest {
    private val testSubject = Decrypter()

    @Test
    fun `Decrypt data`() {
        val data =
            "{\"__uncategorized\":\"9e2ab7b56f5ac18e04f7375bf71891ebb774f8cb2655a90afbc44a60e143067036ba877c0fc40c7eaf165a5199c929a3ae8aa80dabab83f4b5e72cea94b36b376ae5e9e18b56183c24ebef00078dd6de035f8008b8ab30a35ab33fb1aead70e0a72fa3964e3095f1a0432453ad911c6ff8a40c6f4187e911910f8f879d4cadcf34bf7a56af9b2bad3f15f42fbbd343851bb031bbc93995ed5843de202f1964f7bb0e21b1b6eff0353b28f1ed2eb86da300363d9b17d0d2a3a2135b9214442f7ac4e6974cab4f2425222e8a4a5f54e82c3fd6d9dd31ed486861bf7cc6e0d2bf9d4da610bd0f702813ba624e0ce09ab31ec706dd01f1473ed8cf9b44f74712946863f599c145b78167e76b0ce6b3746ea3cedc8278365dc0dbd95da87864f2bced21fbfa86b63c87edd96822052d2f6c3f4e91cb673cac0dbc79fc2ee5dc1110a96f18a54295a6892d42f8353548595895d20f0bf8304f086c80694c4e143fa4117117551cc7dfebc6c1576c8b259e411d84d1f9f117b98f8183698181115bf86dd748b5cfc3009d4e925ddc7d8eb41ada7ad9b1163c2cd1f0e45e\"}"
        val key = "0000000000000000000000000000000000000000000000000000000000000000"
        assertEquals(
            """{"event_type":"UPDATE_USER_STATE_CUSTOM_FIELDS","account_id":"0x0d24186eb41ee244e570679c2489c62ad9a5659c643a17e4a03f04643b95a6c5","session_id":"7b530fc0-71c1-11eb-b6f4-d55aad704f2c","signature":"STUB_SIGNATURE","payload":{"points":2280},"app_id":"242","generated":false,"id":"9c1a68f0-71c3-11eb-b609-89b7e55b27fb","timestamp":"2021-02-18T08:30:51.647Z","is_debug":false}""",
            testSubject.decrypt(data, key)
        )
    }
}
