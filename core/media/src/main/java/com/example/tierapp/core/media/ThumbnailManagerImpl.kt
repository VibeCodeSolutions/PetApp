package com.example.tierapp.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThumbnailManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ThumbnailManager {

    override fun generateThumbs(sourceUri: Uri): ThumbnailManager.ThumbnailResult {
        val thumbDir = File(context.filesDir, "thumbs").also { it.mkdirs() }
        val baseName = UUID.randomUUID().toString()
        val small = generateThumb(sourceUri, sizePx = 150, dest = File(thumbDir, "s_$baseName.jpg"))
        val medium = generateThumb(sourceUri, sizePx = 400, dest = File(thumbDir, "m_$baseName.jpg"))
        return ThumbnailManager.ThumbnailResult(
            thumbSmallPath = small.absolutePath,
            thumbMediumPath = medium.absolutePath,
        )
    }

    private fun generateThumb(uri: Uri, sizePx: Int, dest: File): File {
        val source = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: error("Cannot decode image from URI: $uri")

        val (srcW, srcH) = source.width to source.height
        val cropSize = minOf(srcW, srcH)
        val cropped = Bitmap.createBitmap(source, (srcW - cropSize) / 2, (srcH - cropSize) / 2, cropSize, cropSize)
        source.recycle()

        val scaled = Bitmap.createScaledBitmap(cropped, sizePx, sizePx, true)
        if (cropped !== source) cropped.recycle()

        dest.outputStream().use { out -> scaled.compress(Bitmap.CompressFormat.JPEG, 85, out) }
        scaled.recycle()

        return dest
    }
}
