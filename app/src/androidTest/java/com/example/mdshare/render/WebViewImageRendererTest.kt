package com.example.mdshare.render

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.test.core.app.ActivityScenario
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.min
import com.example.mdshare.MainActivity
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebViewImageRendererTest {
    @Test
    fun renders_bitmap_for_markdown_html() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val html = MarkdownPipeline().buildHtml(
            """
            # Title

            |A|B|
            |---|---|
            |1|2|
            """.trimIndent()
        )

        val result = WebViewImageRenderer(context).renderBlocking(html).bitmap

        assertTrue(result.width > 0)
        assertTrue(result.height > 0)
        assertBitmapContainsVisibleContent(result)
    }

    @Test
    fun renders_bitmap_for_long_code_block_without_timing_out() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val longCodeLine = (1..24).joinToString(" + ") { "segment$it" }
        val html = MarkdownPipeline().buildHtml(
            """
            # Long Code

            ```kotlin
            fun composeLongLine(): String = $longCodeLine
            ```
            """.trimIndent()
        )

        val result = WebViewImageRenderer(context).renderBlocking(html).bitmap

        assertTrue(result.width > 0)
        assertTrue(result.height > 0)
        assertBitmapContainsVisibleContent(result)
    }

    @Test
    fun renders_bitmap_for_user_markdown_summary_without_timing_out() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val html = MarkdownPipeline().buildHtml(
            """
            # 每日总结

            今天完成了 Markdown 渲染方案评估。

            | 方案 | 成本 | 适用场景 | 风险 |
            | --- | --- | --- | --- |
            | 本地渲染 | 低 | MVP | 复杂表格适配 |
            | 云端渲染 | 中 | 一致性强 | 网络依赖 |

            ```text
            Next step:
            - preview
            - export
            - share
            ```

            ```kotlin
            fun render(markdown: String): String {
                require(markdown.isNotBlank())
                return markdown.trim()
            }
            ```
            """.trimIndent()
        )

        val result = WebViewImageRenderer(context).renderBlocking(html).bitmap

        assertTrue(result.width > 0)
        assertTrue(result.height > 0)
        assertBitmapContainsVisibleContent(result)
    }

    @Test
    fun capture_existing_webview_uses_full_scale_without_large_blank_tail() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val html = MarkdownPipeline().buildHtml(
            """
            # 每日总结

            今天完成了 Markdown 渲染方案评估。

            | 方案 | 成本 | 适用场景 | 风险 |
            | --- | --- | --- | --- |
            | 本地渲染 | 低 | MVP | 复杂表格适配 |
            | 云端渲染 | 中 | 一致性强 | 网络依赖 |

            ```python
            import time
            import cv2
            import numpy as np
            from auto_flip_ana import needs_horizontal_flip_v2
            ```
            """.trimIndent()
        )
        val webViewRef = arrayOfNulls<WebView>(1)
        val readyLatch = CountDownLatch(1)
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val webView = WebView(activity).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        view?.postDelayed({ readyLatch.countDown() }, 300L)
                    }
                }
                layout(0, 0, 412, 1400)
                loadDataWithBaseURL(
                    "file:///android_asset/render/",
                    html,
                    "text/html",
                    "utf-8",
                    null
                )
            }
            activity.setContentView(webView)
            webViewRef[0] = webView
        }

        try {
            assertTrue(readyLatch.await(6, TimeUnit.SECONDS))
            val result = WebViewImageRenderer(context).captureExistingWebViewBlocking(checkNotNull(webViewRef[0])).bitmap

            assertTrue(result.width > 0)
            assertTrue(result.height > 0)
            assertBottomAreaIsNotMostlyBlank(result)
        } finally {
            scenario.close()
        }
    }

    @Test
    fun export_mode_places_card_at_full_canvas_origin_after_narrow_preview() {
        val html = MarkdownPipeline().buildHtml(
            """
            # 每日总结

            今天完成了 Markdown 渲染方案评估。
            """.trimIndent()
        )
        val webViewRef = arrayOfNulls<WebView>(1)
        val readyLatch = CountDownLatch(1)
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val webView = WebView(activity).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        view?.postDelayed({ readyLatch.countDown() }, 300L)
                    }
                }
                layout(0, 0, 412, 1400)
                loadDataWithBaseURL(
                    "file:///android_asset/render/",
                    html,
                    "text/html",
                    "utf-8",
                    null
                )
            }
            activity.setContentView(webView)
            webViewRef[0] = webView
        }

        try {
            assertTrue(readyLatch.await(6, TimeUnit.SECONDS))
            val exportedLayout = evaluateJavascriptBlocking(
                webView = checkNotNull(webViewRef[0]),
                script = """
                    (function() {
                      window.__MD_SHARE_SET_EXPORT_MODE__(true);
                      var card = document.querySelector('.render-card');
                      var stage = document.querySelector('.render-stage');
                      var rect = card.getBoundingClientRect();
                      var stageRect = stage.getBoundingClientRect();
                      return Math.round(rect.left) + "|" +
                        Math.round(rect.width) + "|" +
                        Math.round(stageRect.width) + "|" +
                        window.getComputedStyle(stage).overflow;
                    })();
                """.trimIndent()
            )

            assertEquals("0|1080|1080|visible", exportedLayout)
        } finally {
            scenario.close()
        }
    }

    @Test
    fun capture_existing_webview_ignores_preview_scroll_position() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val html = MarkdownPipeline().buildHtml(
            """
            # 顶部标题

            顶部内容应该出现在导出图片中。

            ## 中间内容

            ${List(30) { "- 列表项目 ${it + 1}" }.joinToString("\n")}
            """.trimIndent()
        )
        val webViewRef = arrayOfNulls<WebView>(1)
        val readyLatch = CountDownLatch(1)
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val webView = WebView(activity).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        view?.postDelayed({ readyLatch.countDown() }, 300L)
                    }
                }
                layout(0, 0, 412, 1400)
                loadDataWithBaseURL(
                    "file:///android_asset/render/",
                    html,
                    "text/html",
                    "utf-8",
                    null
                )
            }
            activity.setContentView(webView)
            webViewRef[0] = webView
        }

        try {
            assertTrue(readyLatch.await(6, TimeUnit.SECONDS))
            val webView = checkNotNull(webViewRef[0])
            val scrollLatch = CountDownLatch(1)
            webView.post {
                webView.scrollTo(0, 900)
                scrollLatch.countDown()
            }
            assertTrue(scrollLatch.await(6, TimeUnit.SECONDS))

            val result = WebViewImageRenderer(context).captureExistingWebViewBlocking(webView).bitmap

            assertTopAreaContainsCardBackground(result)
            assertEquals(900, webView.scrollY)
        } finally {
            scenario.close()
        }
    }

    private fun assertBitmapContainsVisibleContent(bitmap: android.graphics.Bitmap) {
        val sampleWidth = min(bitmap.width, 40)
        val sampleHeight = min(bitmap.height, 40)
        var hasNonTransparentPixel = false
        var hasNonWhitePixel = false

        for (x in 0 until sampleWidth) {
            for (y in 0 until sampleHeight) {
                val pixel = bitmap.getPixel(x, y)
                if (android.graphics.Color.alpha(pixel) != 0) {
                    hasNonTransparentPixel = true
                }
                if (pixel != android.graphics.Color.WHITE) {
                    hasNonWhitePixel = true
                }
            }
        }

        assertTrue(hasNonTransparentPixel)
        assertNotEquals(false, hasNonWhitePixel)
    }

    private fun assertBottomAreaIsNotMostlyBlank(bitmap: android.graphics.Bitmap) {
        val startY = (bitmap.height * 0.75f).toInt().coerceAtMost(bitmap.height - 1)
        var coloredPixels = 0
        var sampledPixels = 0

        for (x in 0 until bitmap.width step maxOf(1, bitmap.width / 40)) {
            for (y in startY until bitmap.height step maxOf(1, (bitmap.height - startY) / 40)) {
                sampledPixels++
                if (bitmap.getPixel(x, y) != android.graphics.Color.parseColor("#EEF2FF")) {
                    coloredPixels++
                }
            }
        }

        assertTrue("bottom area is mostly blank", coloredPixels > sampledPixels / 20)
    }

    @Test
    fun capture_existing_webview_shows_full_card_not_just_top_left() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val html = MarkdownPipeline().buildHtml(
            """
            # Top Heading

            Body paragraph with enough text to ensure the card has reasonable width.

            | A | B | C | D |
            | --- | --- | --- | --- |
            | 1 | 2 | 3 | 4 |

            ## Bottom Section

            Final paragraph at the bottom of the content.
            """.trimIndent()
        )
        val webViewRef = arrayOfNulls<WebView>(1)
        val readyLatch = CountDownLatch(1)
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val webView = WebView(activity).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        view?.postDelayed({ readyLatch.countDown() }, 300L)
                    }
                }
                layout(0, 0, 412, 1400)
                loadDataWithBaseURL(
                    "file:///android_asset/render/",
                    html,
                    "text/html",
                    "utf-8",
                    null
                )
            }
            activity.setContentView(webView)
            webViewRef[0] = webView
        }

        try {
            assertTrue(readyLatch.await(6, TimeUnit.SECONDS))
            val bitmap = WebViewImageRenderer(context)
                .captureExistingWebViewBlocking(checkNotNull(webViewRef[0]))
                .bitmap

            assertTrue(bitmap.width > 0)
            assertTrue(bitmap.height > 0)
            assertCardRightEdgeIsVisible(bitmap)
        } finally {
            scenario.close()
        }
    }

    private fun assertCardRightEdgeIsVisible(bitmap: android.graphics.Bitmap) {
        val rightEdgeStart = bitmap.width - 56
        var whitePixels = 0
        var sampled = 0

        for (x in rightEdgeStart until bitmap.width step 4) {
            for (y in 60 until min(bitmap.height, 400) step 8) {
                sampled++
                if (bitmap.getPixel(x, y) == android.graphics.Color.WHITE) {
                    whitePixels++
                }
            }
        }

        assertTrue(
            "Card right edge (white padding) not visible — likely capturing only top-left corner. " +
            "White pixels: $whitePixels / $sampled",
            whitePixels > sampled / 4
        )
    }

    private fun assertTopAreaContainsCardBackground(bitmap: android.graphics.Bitmap) {
        var whitePixels = 0
        var sampledPixels = 0

        for (x in 0 until bitmap.width step maxOf(1, bitmap.width / 40)) {
            for (y in 0 until (bitmap.height * 0.12f).toInt().coerceAtLeast(1) step maxOf(1, bitmap.height / 100)) {
                sampledPixels++
                if (bitmap.getPixel(x, y) == android.graphics.Color.WHITE) {
                    whitePixels++
                }
            }
        }

        assertTrue("top area does not contain the exported card", whitePixels > sampledPixels / 3)
    }

    private fun evaluateJavascriptBlocking(webView: WebView, script: String): String {
        val latch = CountDownLatch(1)
        var result = ""

        webView.post {
            webView.evaluateJavascript(script) { rawValue ->
                result = rawValue
                    ?.removePrefix("\"")
                    ?.removeSuffix("\"")
                    ?.replace("\\\"", "\"")
                    ?.trim()
                    .orEmpty()
                latch.countDown()
            }
        }

        assertTrue(latch.await(6, TimeUnit.SECONDS))
        return result
    }
}
