package com.example.mdshare.share

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ShareIntentParserTest {
    @Test
    fun `extracts shared text from ACTION_SEND`() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "# Title")
        }

        assertEquals("# Title", ShareIntentParser.extractText(intent))
    }

    @Test
    fun `extracts process text payload and trims it`() {
        val intent = Intent(Intent.ACTION_PROCESS_TEXT).apply {
            putExtra(Intent.EXTRA_PROCESS_TEXT, "  plain text  ")
        }

        assertEquals("plain text", ShareIntentParser.extractText(intent))
    }

    @Test
    fun `returns null when payload missing or blank`() {
        assertNull(ShareIntentParser.extractText(Intent(Intent.ACTION_SEND)))

        val blank = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, "   ")
        }
        assertNull(ShareIntentParser.extractText(blank))
    }
}
