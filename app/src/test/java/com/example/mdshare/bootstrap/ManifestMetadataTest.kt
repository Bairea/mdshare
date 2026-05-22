package com.example.mdshare.bootstrap

import android.graphics.BitmapFactory
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

@RunWith(RobolectricTestRunner::class)
class ManifestMetadataTest {
    @Test
    fun `manifest declares app name and launcher icons`() {
        val manifest = readXml(File("src/main/AndroidManifest.xml"))
        val application = manifest.documentElement
            .getElementsByTagName("application")
            .item(0) as Element

        assertEquals("@string/app_name", application.getAttribute("android:label"))
        assertEquals("@mipmap/ic_launcher", application.getAttribute("android:icon"))
        assertEquals("@mipmap/ic_launcher_round", application.getAttribute("android:roundIcon"))
    }

    @Test
    fun `app name resource is mdshare`() {
        val stringsFile = File("src/main/res/values/strings.xml")
        val strings = readXml(stringsFile)
        val appName = strings.documentElement
            .getElementsByTagName("string")
            .let { nodes ->
                (0 until nodes.length)
                    .map { nodes.item(it) as Element }
                    .first { it.getAttribute("name") == "app_name" }
            }

        assertEquals("mdshare", appName.textContent)
    }

    @Test
    fun `launcher icon resources exist`() {
        assertTrue(File("src/main/res/mipmap-mdpi/ic_launcher.png").isFile)
        assertTrue(File("src/main/res/mipmap-hdpi/ic_launcher.png").isFile)
        assertTrue(File("src/main/res/mipmap-xhdpi/ic_launcher.png").isFile)
        assertTrue(File("src/main/res/mipmap-xxhdpi/ic_launcher.png").isFile)
        assertTrue(File("src/main/res/mipmap-xxxhdpi/ic_launcher.png").isFile)
    }

    @Test
    fun `launcher icons use adaptive icon configuration`() {
        val adaptiveIconXml = File("src/main/res/mipmap-anydpi-v26/ic_launcher.xml")
        assertTrue("adaptive icon XML is missing", adaptiveIconXml.isFile)
        val xmlContent = adaptiveIconXml.readText()
        assertTrue(xmlContent.contains("adaptive-icon"))
        assertTrue(xmlContent.contains("ic_launcher_background"))
        assertTrue(xmlContent.contains("ic_launcher_foreground"))
        assertTrue(File("src/main/res/drawable/ic_launcher_background.xml").isFile)
        assertTrue(File("src/main/res/drawable/ic_launcher_foreground.xml").isFile)
    }

    @Test
    fun `fallback png icons have rounded rectangle shape`() {
        listOf(
            "src/main/res/mipmap-mdpi/ic_launcher.png",
            "src/main/res/mipmap-hdpi/ic_launcher.png",
            "src/main/res/mipmap-xhdpi/ic_launcher.png",
            "src/main/res/mipmap-xxhdpi/ic_launcher.png",
            "src/main/res/mipmap-xxxhdpi/ic_launcher.png"
        ).forEach { path ->
            val image = BitmapFactory.decodeFile(path)
            val w = image.width
            val h = image.height

            // Corners are transparent (rounded shape)
            assertTrue("$path TL corner not transparent", alphaAt(image.getPixel(0, 0)) < 16)
            assertTrue("$path TR corner not transparent", alphaAt(image.getPixel(w - 1, 0)) < 16)
            assertTrue("$path BL corner not transparent", alphaAt(image.getPixel(0, h - 1)) < 16)
            assertTrue("$path BR corner not transparent", alphaAt(image.getPixel(w - 1, h - 1)) < 16)

            // Center is fully opaque (content present)
            assertTrue("$path center not opaque", alphaAt(image.getPixel(w / 2, h / 2)) == 255)
        }
    }

    private fun readXml(file: File) = DocumentBuilderFactory
        .newInstance()
        .newDocumentBuilder()
        .parse(file)

    private fun alphaAt(argb: Int): Int = argb ushr 24
}
