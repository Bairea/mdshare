package com.example.mdshare.render

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class RenderAssetsTest {
    @Test
    fun `render js marks wide tables as compact`() {
        val script = File("src/main/assets/render/render.js").readText()

        assertTrue(script.contains("compact-table"))
        assertTrue(script.contains("querySelectorAll(\"table\")"))
    }

    @Test
    fun `render js exposes a render ready signal`() {
        val script = File("src/main/assets/render/render.js").readText()

        assertTrue(script.contains("__MD_SHARE_RENDER_READY__"))
        assertTrue(script.contains("setTimeout"))
        assertTrue(script.contains("__MD_SHARE_SET_EXPORT_MODE__"))
    }

    @Test
    fun `render css separates typography for body code and tables`() {
        val css = File("src/main/assets/render/render.css").readText()

        assertTrue(css.contains("font-size: 18px"))
        assertTrue(css.contains("h1 {"))
        assertTrue(css.contains("font-size: 48px"))
        assertTrue(css.contains("h2 {"))
        assertTrue(css.contains("font-size: 38px"))
        assertTrue(css.contains("--render-scale"))
        assertTrue(css.contains("transform: scale(var(--render-scale, 1))"))
        assertTrue(css.contains("font-family: \"Cascadia Code\""))
        assertTrue(css.contains("max-width: 100ch"))
        assertTrue(css.contains("overflow-wrap: break-word"))
        assertTrue(css.contains("table.compact-table"))
        assertTrue(css.contains("table:not(.compact-table)"))
        assertTrue(css.contains("font-size: 15px"))
        assertTrue(css.contains("font-size: 13px"))
        assertTrue(css.contains("font-size: 13px"))
        assertTrue(css.contains("table.compact-table"))
    }

    @Test
    fun `highlight theme defines token colors`() {
        val css = File("src/main/assets/render/highlight-theme.css").readText()

        assertTrue(css.contains(".hljs-keyword"))
        assertTrue(css.contains(".hljs-string"))
        assertTrue(css.contains(".hljs-title"))
        assertTrue(css.contains(".hljs-comment"))
    }

    @Test
    fun `highlight script emits syntax token spans`() {
        val script = File("src/main/assets/render/highlight.min.js").readText()

        assertTrue(script.contains("hljs-keyword"))
        assertTrue(script.contains("class=\"hljs-"))
        assertTrue(script.contains("innerHTML"))
        assertTrue(script.contains("textContent"))
    }

    @Test
    fun `export mode has fixed canvas layout rules`() {
        val css = File("src/main/assets/render/render.css").readText()

        assertTrue(css.contains(".md-share-export-mode .render-stage"))
        assertTrue(css.contains("width: 1080px"))
        assertTrue(css.contains("overflow: visible"))
        assertTrue(css.contains("transform: none"))
    }
}
