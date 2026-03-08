package com.example.tierapp.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
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
        // EXIF einmalig lesen — gilt für beide Thumbnails (spart 2 I/O-Operationen)
        val exifOrientation = readExifOrientation(sourceUri)
        val small = generateThumb(sourceUri, sizePx = 150, dest = File(thumbDir, "s_$baseName.jpg"), exifOrientation = exifOrientation)
        val medium = generateThumb(sourceUri, sizePx = 400, dest = File(thumbDir, "m_$baseName.jpg"), exifOrientation = exifOrientation)
        return ThumbnailManager.ThumbnailResult(
            thumbSmallPath = small.absolutePath,
            thumbMediumPath = medium.absolutePath,
        )
    }

    private fun readExifOrientation(uri: Uri): Int =
        context.contentResolver.openInputStream(uri)?.use { stream ->
            ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        } ?: ExifInterface.ORIENTATION_NORMAL

    private fun generateThumb(uri: Uri, sizePx: Int, dest: File, exifOrientation: Int): File {
        // Pass 1: Abmessungen ermitteln ohne das Bitmap in den Speicher zu laden
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, opts)
        }

        opts.inSampleSize = calculateInSampleSize(opts.outWidth, opts.outHeight, sizePx)
        opts.inJustDecodeBounds = false

        // Pass 2: Speicheroptimierter Decode mit berechneter Subsampling-Rate
        val source = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, opts)
        } ?: error("Cannot decode image from URI: $uri")

        // EXIF-Orientierung anwenden (Orientation bereits gecacht, kein weiterer I/O)
        val oriented = applyExifRotationWithOrientation(source, exifOrientation)

        val (srcW, srcH) = oriented.width to oriented.height
        val cropSize = minOf(srcW, srcH)
        val cropped = Bitmap.createBitmap(oriented, (srcW - cropSize) / 2, (srcH - cropSize) / 2, cropSize, cropSize)
        if (oriented !== source) source.recycle()
        if (cropped !== oriented) oriented.recycle()

        val scaled = Bitmap.createScaledBitmap(cropped, sizePx, sizePx, true)
        if (cropped !== scaled) cropped.recycle()

        dest.outputStream().use { out -> scaled.compress(Bitmap.CompressFormat.JPEG, 85, out) }
        scaled.recycle()

        return dest
    }

    /**
     * Wendet EXIF-Orientierung als int direkt an — ohne erneuten ContentResolver-Aufruf.
     * Das Original-Bitmap wird recycelt wenn ein neues erstellt wird.
     */
    private fun applyExifRotationWithOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap // ORIENTATION_NORMAL oder unbekannt — kein Transform nötig
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            .also { bitmap.recycle() }
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqSizePx: Int): Int {
        var inSampleSize = 1
        val smallestSide = minOf(width, height)
        while (smallestSide / (inSampleSize * 2) >= reqSizePx) {
            inSampleSize *= 2
        }
        return inSampleSize
    }
}
