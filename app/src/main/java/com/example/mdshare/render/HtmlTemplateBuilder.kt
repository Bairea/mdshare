package com.example.mdshare.render

class HtmlTemplateBuilder(
    private val theme: RenderTheme
) {
    fun wrap(bodyHtml: String): String = """
        <!DOCTYPE html>
        <html lang="zh-CN">
        <head>
          <meta charset="utf-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <link rel="stylesheet" href="file:///android_asset/render/highlight-theme.css" />
          <link rel="stylesheet" href="file:///android_asset/render/render.css" />
        </head>
        <body>
          <div class="render-stage">
            <main class="render-card" style="width:${theme.canvasWidthPx}px">
              <div class="render-content">$bodyHtml</div>
            </main>
          </div>
          <script src="file:///android_asset/render/highlight.min.js"></script>
          <script src="file:///android_asset/render/render.js"></script>
        </body>
        </html>
    """.trimIndent()
}
