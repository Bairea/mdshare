package com.example.mdshare.render

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.example.mdshare.model.RenderResult
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.max

class WebViewImageRenderer(
    private val context: Context,
    private val theme: RenderTheme = RenderTheme()
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @SuppressLint("SetJavaScriptEnabled")
    fun renderBlocking(html: String): RenderResult {
        check(Looper.myLooper() != Looper.getMainLooper()) {
            "renderBlocking must not run on the main thread"
        }

        val latch = CountDownLatch(1)
        var result: RenderResult? = null
        var failure: Throwable? = null

        mainHandler.post {
            val webView = WebView(context)
            val isCompleted = AtomicBoolean(false)
            fun complete(renderResult: RenderResult? = null, throwable: Throwable? = null) {
                if (!isCompleted.compareAndSet(false, true)) return
                result = renderResult
                failure = throwable
                webView.destroy()
                latch.countDown()
            }
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.useWideViewPort = true
            webView.settings.loadWithOverviewMode = true
            webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
            webView.setBackgroundColor(Color.WHITE)
            webView.clearCache(true)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val safeView = view ?: run {
                        complete(throwable = IllegalStateException("WebView became null after load"))
                        return
                    }
                    awaitRenderReady(
                        webView = safeView,
                        onReady = {
                            renderToBitmap(
                                webView = safeView,
                                onSuccess = { complete(renderResult = it) },
                                onFailure = { complete(throwable = it) }
                            )
                        },
                        onFailure = { complete(throwable = it) }
                    )
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    if (request?.isForMainFrame == true) {
                        complete(
                            throwable = IllegalStateException(
                                "Failed to load HTML: ${error?.description ?: "unknown error"}"
                            )
                        )
                    }
                }
            }
            webView.loadDataWithBaseURL(
                "file:///android_asset/render/",
                html,
                "text/html",
                "utf-8",
                null
            )
        }

        check(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            "Timed out while rendering HTML in WebView"
        }

        failure?.let { throw IllegalStateException("Failed to render HTML", it) }
        return checkNotNull(result)
    }

    fun captureExistingWebViewBlocking(webView: WebView): RenderResult {
        check(Looper.myLooper() != Looper.getMainLooper()) {
            "captureExistingWebViewBlocking must not run on the main thread"
        }

        val latch = CountDownLatch(1)
        var result: RenderResult? = null
        var failure: Throwable? = null

        mainHandler.post {
            captureExistingWebView(
                webView = webView,
                onSuccess = {
                    result = it
                    latch.countDown()
                },
                onFailure = {
                    failure = it
                    latch.countDown()
                }
            )
        }

        check(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            "Timed out while capturing rendered WebView"
        }

        failure?.let { throw IllegalStateException("Failed to capture rendered WebView", it) }
        return checkNotNull(result)
    }

    private fun captureExistingWebView(
        webView: WebView,
        onSuccess: (RenderResult) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        setExportMode(
            webView = webView,
            enabled = true,
            onSuccess = {
                awaitRenderReady(
                    webView = webView,
                    onReady = {
                        renderToBitmap(
                            webView = webView,
                            onSuccess = { renderResult ->
                                restorePreviewMode(
                                    webView = webView,
                                    renderResult = renderResult,
                                    onSuccess = onSuccess,
                                    onFailure = onFailure
                                )
                            },
                            onFailure = { throwable ->
                                restorePreviewMode(
                                    webView = webView,
                                    throwable = throwable,
                                    onSuccess = onSuccess,
                                    onFailure = onFailure
                                )
                            }
                        )
                    },
                    onFailure = { throwable ->
                        restorePreviewMode(
                            webView = webView,
                            throwable = throwable,
                            onSuccess = onSuccess,
                            onFailure = onFailure
                        )
                    }
                )
            },
            onFailure = onFailure
        )
    }

    private fun awaitRenderReady(
        webView: WebView,
        onReady: () -> Unit,
        onFailure: (Throwable) -> Unit,
        previousHeight: Int? = null,
        stablePasses: Int = 0,
        startedAtMs: Long = System.currentTimeMillis()
    ) {
        if (System.currentTimeMillis() - startedAtMs >= RENDER_READY_TIMEOUT_MS) {
            onFailure(IllegalStateException("Timed out while waiting for WebView content to settle"))
            return
        }

        webView.evaluateJavascript(RENDER_STATUS_SCRIPT) { rawValue ->
            val status = RenderStatus.fromJavascriptResult(rawValue)
            val isStableHeight = previousHeight != null && kotlin.math.abs(status.height - previousHeight) <= 1
            val domReady = status.ready || status.readyState == "complete"
            val nextStablePasses = if (domReady && status.height > 0 && isStableHeight) {
                stablePasses + 1
            } else {
                0
            }

            if (domReady && status.height > 0 && nextStablePasses >= REQUIRED_STABLE_PASSES) {
                webView.postDelayed(onReady, CAPTURE_DELAY_MS)
            } else {
                webView.postDelayed(
                    {
                        awaitRenderReady(
                            webView = webView,
                            onReady = onReady,
                            onFailure = onFailure,
                            previousHeight = status.height.takeIf { it > 0 } ?: previousHeight,
                            stablePasses = nextStablePasses,
                            startedAtMs = startedAtMs
                        )
                    },
                    READINESS_POLL_INTERVAL_MS
                )
            }
        }
    }

    private fun setExportMode(
        webView: WebView,
        enabled: Boolean,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val script = if (enabled) ENABLE_EXPORT_MODE_SCRIPT else DISABLE_EXPORT_MODE_SCRIPT
        webView.evaluateJavascript(script) { rawValue ->
            val result = rawValue
                ?.removePrefix("\"")
                ?.removeSuffix("\"")
                ?.replace("\\\"", "\"")
                ?.trim()
                .orEmpty()
            if (result == "missing") {
                onFailure(IllegalStateException("Render page does not expose export mode controls"))
            } else {
                webView.postDelayed(onSuccess, CAPTURE_DELAY_MS)
            }
        }
    }

    private fun restorePreviewMode(
        webView: WebView,
        renderResult: RenderResult? = null,
        throwable: Throwable? = null,
        onSuccess: (RenderResult) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        setExportMode(
            webView = webView,
            enabled = false,
            onSuccess = {
                renderResult?.let(onSuccess) ?: onFailure(
                    throwable ?: IllegalStateException("Failed to capture rendered WebView")
                )
            },
            onFailure = { resetFailure ->
                renderResult?.let(onSuccess) ?: onFailure(throwable ?: resetFailure)
            }
        )
    }

    private fun renderToBitmap(
        webView: WebView,
        onSuccess: (RenderResult) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val drawContent = {
            readCaptureHeight(
                webView = webView,
                onSuccess = { captureHeightPx ->
                    try {
                        val renderResult = webView.captureBitmap(
                            targetWidth = theme.canvasWidthPx,
                            targetHeightHint = captureHeightPx
                        )
                        onSuccess(renderResult)
                    } catch (throwable: Throwable) {
                        onFailure(throwable)
                    }
                },
                onFailure = onFailure
            )
        }

        if (WebViewFeature.isFeatureSupported(WebViewFeature.VISUAL_STATE_CALLBACK)) {
            WebViewCompat.postVisualStateCallback(
                webView,
                System.nanoTime(),
                object : WebViewCompat.VisualStateCallback {
                    override fun onComplete(requestId: Long) {
                        drawContent()
                    }
                }
            )
        } else {
            webView.post(drawContent)
        }
    }

    private fun readCaptureHeight(
        webView: WebView,
        onSuccess: (Int) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        webView.evaluateJavascript(CAPTURE_HEIGHT_SCRIPT) { rawValue ->
            val captureHeight = rawValue
                ?.removePrefix("\"")
                ?.removeSuffix("\"")
                ?.replace("\\\"", "\"")
                ?.trim()
                ?.toIntOrNull()

            if (captureHeight != null && captureHeight > 0) {
                onSuccess(captureHeight)
            } else {
                onFailure(IllegalStateException("Failed to read capture height from rendered page"))
            }
        }
    }

    private fun WebView.captureBitmap(targetWidth: Int, targetHeightHint: Int? = null): RenderResult {
        val widthSpec = android.view.View.MeasureSpec.makeMeasureSpec(
            targetWidth,
            android.view.View.MeasureSpec.EXACTLY
        )
        val unspecifiedHeightSpec = android.view.View.MeasureSpec.makeMeasureSpec(
            0,
            android.view.View.MeasureSpec.UNSPECIFIED
        )
        measure(widthSpec, unspecifiedHeightSpec)

        val contentHeightPx = ceil(contentHeight * resources.displayMetrics.density).toInt()
        val targetHeight = targetHeightHint?.takeIf { it > 0 } ?: max(max(measuredHeight, contentHeightPx), 1)

        val heightSpec = android.view.View.MeasureSpec.makeMeasureSpec(
            targetHeight,
            android.view.View.MeasureSpec.EXACTLY
        )
        measure(widthSpec, heightSpec)
        layout(0, 0, measuredWidth, targetHeight)

        val safeWidth = max(measuredWidth, 1)
        val bitmap = Bitmap.createBitmap(safeWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        draw(canvas)

        return RenderResult(
            bitmap = bitmap,
            width = bitmap.width,
            height = bitmap.height
        )
    }

    private data class RenderStatus(
        val ready: Boolean,
        val height: Int,
        val readyState: String
    ) {
        companion object {
            fun fromJavascriptResult(rawValue: String?): RenderStatus {
                val cleaned = rawValue
                    ?.removePrefix("\"")
                    ?.removeSuffix("\"")
                    ?.replace("\\\"", "\"")
                    ?.trim()
                    .orEmpty()
                val parts = cleaned.split("|")
                val ready = parts.firstOrNull()?.toBooleanStrictOrNull() ?: false
                val height = parts.getOrNull(1)?.toIntOrNull() ?: 0
                val readyState = parts.getOrNull(2).orEmpty()
                return RenderStatus(ready = ready, height = height, readyState = readyState)
            }
        }
    }

    private companion object {
        const val TIMEOUT_SECONDS = 20L
        const val CAPTURE_DELAY_MS = 150L
        const val READINESS_POLL_INTERVAL_MS = 120L
        const val RENDER_READY_TIMEOUT_MS = 15_000L
        const val REQUIRED_STABLE_PASSES = 2
        const val ENABLE_EXPORT_MODE_SCRIPT = """
            (function() {
              if (typeof window.__MD_SHARE_SET_EXPORT_MODE__ !== "function") {
                return "missing";
              }
              return window.__MD_SHARE_SET_EXPORT_MODE__(true);
            })();
        """
        const val DISABLE_EXPORT_MODE_SCRIPT = """
            (function() {
              if (typeof window.__MD_SHARE_SET_EXPORT_MODE__ !== "function") {
                return "missing";
              }
              return window.__MD_SHARE_SET_EXPORT_MODE__(false);
            })();
        """
        const val CAPTURE_HEIGHT_SCRIPT = """
            (function() {
              var card = document.querySelector('.render-card');
              if (!card) {
                var fallbackDoc = document.documentElement;
                var fallbackBody = document.body;
                return String(Math.max(
                  fallbackDoc ? fallbackDoc.scrollHeight : 0,
                  fallbackBody ? fallbackBody.scrollHeight : 0
                ));
              }
              var rect = card.getBoundingClientRect();
              var style = window.getComputedStyle(card);
              var marginBottom = parseFloat(style.marginBottom || "0") || 0;
              var bottom = rect.bottom + window.scrollY + marginBottom;
              return String(Math.ceil(bottom));
            })();
        """
        const val RENDER_STATUS_SCRIPT = """
            (function() {
              var doc = document.documentElement;
              var body = document.body;
              var card = document.querySelector('.render-card');
              var height = Math.max(
                doc ? doc.scrollHeight : 0,
                body ? body.scrollHeight : 0,
                card ? card.scrollHeight : 0
              );
              return (!!window.__MD_SHARE_RENDER_READY__) + "|" + height + "|" + document.readyState;
            })();
        """
    }
}
