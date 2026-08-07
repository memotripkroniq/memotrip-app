package com.example.memotrip_kroniq.data.image

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream

object ImageUploadNormalizer {

    private const val MAX_LONGEST_SIDE = 2560
    private const val JPEG_QUALITY = 88
    private const val OUTPUT_MIME_TYPE = "image/jpeg"

    fun normalize(
        contentResolver: ContentResolver,
        uri: Uri,
        filenamePrefix: String
    ): NormalizedUploadImage {
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        val boundsStream = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Cannot open image for reading.")

        boundsStream.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        }

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IllegalStateException("Selected file is not a readable image.")
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_LONGEST_SIDE)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val decodedBitmap = contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: throw IllegalStateException("Cannot open image for decoding.")

        try {
            val exifOrientation = contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL

            val orientedBitmap = applyExifOrientation(decodedBitmap, exifOrientation)
            val resizedBitmap = resizeIfNeeded(orientedBitmap, MAX_LONGEST_SIDE)
            val jpegReadyBitmap = prepareForJpegCompression(resizedBitmap)

            val outputBytes = try {
                ByteArrayOutputStream().use { output ->
                    if (!jpegReadyBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                        throw IllegalStateException("Selected image could not be converted to JPEG.")
                    }
                    output.toByteArray()
                }
            } finally {
                if (jpegReadyBitmap !== resizedBitmap) {
                    jpegReadyBitmap.recycle()
                }
                if (resizedBitmap !== orientedBitmap) {
                    resizedBitmap.recycle()
                }
                if (orientedBitmap !== decodedBitmap) {
                    orientedBitmap.recycle()
                }
            }

            if (outputBytes.isEmpty()) {
                throw IllegalStateException("Selected image could not be converted to JPEG.")
            }

            return NormalizedUploadImage(
                bytes = outputBytes,
                mimeType = OUTPUT_MIME_TYPE,
                filename = "$filenamePrefix.jpg"
            )
        } finally {
            decodedBitmap.recycle()
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxLongestSide: Int): Int {
        var sampleSize = 1
        val longestSide = maxOf(width, height)

        while (longestSide / (sampleSize * 2) >= maxLongestSide) {
            sampleSize *= 2
        }

        return sampleSize
    }

    private fun prepareForJpegCompression(bitmap: Bitmap): Bitmap {
        if (!bitmap.hasAlpha()) {
            return bitmap
        }

        return Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888).also { flattened ->
            Canvas(flattened).apply {
                drawColor(Color.WHITE)
                drawBitmap(bitmap, 0f, 0f, null)
            }
        }
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    postRotate(90f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    postRotate(270f)
                    postScale(-1f, 1f)
                }
            }
        }

        if (matrix.isIdentity) {
            return bitmap
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun resizeIfNeeded(bitmap: Bitmap, maxLongestSide: Int): Bitmap {
        val longestSide = maxOf(bitmap.width, bitmap.height)
        if (longestSide <= maxLongestSide) {
            return bitmap
        }

        val scale = maxLongestSide.toFloat() / longestSide.toFloat()
        val targetWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
}
