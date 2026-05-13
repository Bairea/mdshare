package com.example.mdshare.render

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet

class MarkdownPipeline(
    private val theme: RenderTheme = RenderTheme()
) {
    private val options = MutableDataSet().apply {
        set(
            Parser.EXTENSIONS,
            listOf(
                TablesExtension.create(),
                StrikethroughExtension.create()
            )
        )
    }
    private val parser = Parser.builder(options).build()
    private val renderer = HtmlRenderer.builder(options).build()
    private val templateBuilder = HtmlTemplateBuilder(theme)

    fun buildHtml(markdown: String): String {
        val normalizedMarkdown = markdown.trim()
        val document = parser.parse(normalizedMarkdown)
        val bodyHtml = renderer.render(document)
        return templateBuilder.wrap(bodyHtml)
    }
}
