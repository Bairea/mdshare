package com.example.mdshare

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityIntentTest {
    @Test
    fun launches_with_shared_text_intent() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            setClassName("com.example.mdshare", "com.example.mdshare.MainActivity")
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "# Shared")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        ActivityScenario.launch<MainActivity>(intent).use { }
    }
}
