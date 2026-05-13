package com.example.mdshare.share

import android.content.Intent

object ShareIntentParser {
    fun extractText(intent: Intent?): String? {
        if (intent == null) return null

        val rawText = when (intent.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            Intent.ACTION_PROCESS_TEXT -> {
                intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
            }

            else -> null
        }

        return rawText?.trim()?.takeIf { it.isNotEmpty() }
    }
}
