package com.example.mdshare.feature.editor

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorRouteLayoutTest {
    @Test
    fun `editor page uses scrollable container`() {
        val source = File("src/main/java/com/example/mdshare/feature/editor/EditorRoute.kt").readText()

        assertTrue(source.contains("testTag(\"editor_scroll\")"))
        assertTrue(source.contains("AndroidView("))
        assertTrue(source.contains("EditText(context)"))
        assertTrue(source.contains(".height(420.dp)"))
        assertTrue(source.contains("isVerticalScrollBarEnabled = true"))
    }
}
