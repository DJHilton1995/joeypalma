package com.example

import com.example.security.RustyMcpEngine
import com.example.security.SecureVault
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testRustyMcpSealingAndDecryption() {
        val payload = "Hello Joey! RustyMCP Sealed Protocol Test 2026"
        val sealed = RustyMcpEngine.sealClientMessage(payload)

        assertNotNull(sealed.client_ed25519_pk)
        assertNotNull(sealed.client_ephemeral_x25519)
        assertNotNull(sealed.nonce)
        assertNotNull(sealed.ciphertext)
        assertNotNull(sealed.signature)
        assertTrue(sealed.sequence_number > 0)

        val result = RustyMcpEngine.processIncomingMessage(sealed)
        assertTrue(result.isSuccess)
        val verified = result.getOrThrow()
        assertEquals(payload, verified.plaintext)
        assertTrue(verified.isReplayProtected)
        assertTrue(verified.isIntegrityVerified)
    }

    @Test
    fun testRustyMcpAntiReplayAttackDetection() {
        val payload = "Anti-Replay Attack Probe"
        val sealed = RustyMcpEngine.sealClientMessage(payload)

        // First ingestion succeeds
        val result1 = RustyMcpEngine.processIncomingMessage(sealed)
        assertTrue(result1.isSuccess)

        // Second ingestion with same sequence number MUST fail (Replay detected)
        val result2 = RustyMcpEngine.processIncomingMessage(sealed)
        assertTrue(result2.isFailure)
        assertTrue(result2.exceptionOrNull()?.message?.contains("Replay attack detected") == true)
    }

    @Test
    fun testRustyMcpLiveDiagnostics() {
        val diag = RustyMcpEngine.runLiveDiagnostics()
        assertTrue(diag.contains("RustyMCP Protocol OK"))
    }

    @Test
    fun testSecureVaultEncryption() {
        val original = "Joey Palma Secret Key Vault Test"
        val encrypted = SecureVault.encrypt(original)
        val decrypted = SecureVault.decrypt(encrypted)
        assertEquals(original, decrypted)
    }
}
