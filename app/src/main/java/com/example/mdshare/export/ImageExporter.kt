package com.example.mdshare.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore

object ImageExporter {
    fun buildFileName(): String = "md-share-${System.currentTimeMillis()}.png"

    fun saveBitmap(context: Context, bitmap: Bitmap): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, buildFileName())
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/MdShare"
            )
        }

        val uri = checkNotNull(
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        ) {
            "Failed to create MediaStore entry for exported image"
        }

        resolver.openOutputStream(uri)?.use { outputStream ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                "Failed to encode PNG"
            }
        } ?: error("Failed to open output stream for exported image")

        return uri
    }
}
