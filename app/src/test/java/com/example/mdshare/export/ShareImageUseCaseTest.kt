package com.example.mdshare.export

import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ShareImageUseCaseTest {
    @Test
    fun `build intent uses png action stream and read permission`() {
        val uri = Uri.parse("content://mdshare/result.png")

        val intent = ShareImageUseCase.buildIntent(uri)

        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("image/png", intent.type)
        assertEquals(uri, IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java))
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }
}
