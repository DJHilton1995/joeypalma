package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.SecureVault
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Joey AI", appName)
  }

  @Test
  fun `test secure vault aes gcm encryption and checksum`() {
    val originalText = "How you doin'? Super secure Joey message."
    val encrypted = SecureVault.encrypt(originalText)
    assertNotEquals(originalText, encrypted)

    val decrypted = SecureVault.decrypt(encrypted)
    assertEquals(originalText, decrypted)

    val checksum = SecureVault.computeChecksum(originalText)
    assertEquals(16, checksum.length)
  }
}

