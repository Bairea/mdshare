package com.example.mdshare.bootstrap

import android.app.Application
import androidx.compose.ui.graphics.Color
import com.example.mdshare.MdShareApplication
import com.example.mdshare.ui.theme.Accent
import com.example.mdshare.ui.theme.CardBackground
import com.example.mdshare.ui.theme.PrimaryText
import com.example.mdshare.ui.theme.SecondaryText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BootstrapSmokeTest {
    @Test
    fun `application class extends android application`() {
        assertTrue(MdShareApplication() is Application)
    }

    @Test
    fun `theme exposes expected palette`() {
        assertEquals(Color(0xFFF8FAFC), CardBackground)
        assertEquals(Color(0xFF0F172A), PrimaryText)
        assertEquals(Color(0xFF475569), SecondaryText)
        assertEquals(Color(0xFF4F46E5), Accent)
    }
}
