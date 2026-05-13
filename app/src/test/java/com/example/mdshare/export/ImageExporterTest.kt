package com.example.mdshare.export

import org.junit.Assert.assertTrue
import org.junit.Test

class ImageExporterTest {
    @Test
    fun `build file name uses png extension and md-share prefix`() {
        val fileName = ImageExporter.buildFileName()

        assertTrue(fileName.startsWith("md-share-"))
        assertTrue(fileName.endsWith(".png"))
    }
}
