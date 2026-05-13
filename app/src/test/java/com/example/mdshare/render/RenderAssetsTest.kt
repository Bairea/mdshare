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

        assertTrue(css.contains("font-size: 26px"))
        assertTrue(css.contains("--render-scale"))
        assertTrue(css.contains("transform: scale(var(--render-scale, 1))"))
        assertTrue(css.contains("font-family: \"Cascadia Code\""))
        assertTrue(css.contains("max-width: 100ch"))
        assertTrue(css.contains("overflow-wrap: break-word"))
        assertTrue(css.contains("table.compact-table"))
        assertTrue(css.contains("table:not(.compact-table)"))
        assertTrue(css.contains("font-size: 16px"))
        assertTrue(css.contains("font-size: 14px"))
        assertTrue(css.contains("font-size: 14px"))
        assertTrue(css.contains("table.compact-table"))
    }
}
