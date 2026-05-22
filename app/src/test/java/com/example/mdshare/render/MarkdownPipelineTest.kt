package com.example.mdshare.render

import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class MarkdownPipelineTest {
    @Test
    fun `wraps markdown tables and code blocks with render shell`() {
        val html = MarkdownPipeline().buildHtml(
            """
            # Title

            | A | B | C |
            | --- | --- | --- |
            | 1 | 2 | 3 |

            ```kotlin
            fun main() = println("hi")
            ```
            """.trimIndent()
        )

        assertTrue(html.contains("<table"))
        assertTrue(html.contains("class=\"language-kotlin\""))
        assertTrue(html.contains("render-card"))
        assertTrue(html.contains("render-stage"))
        assertTrue(html.contains("highlight.min.js"))
        assertTrue(html.contains("render.css"))
        assertTrue(html.contains("width:1080px"))
    }

    @Test
    fun `does not add a default title label to rendered image html`() {
        val html = MarkdownPipeline().buildHtml(
            """
            # Daily Note

            Body content.
            """.trimIndent()
        )

        assertFalse(html.contains("render-label"))
        assertFalse(html.contains("AI 摘要"))
    }
}
