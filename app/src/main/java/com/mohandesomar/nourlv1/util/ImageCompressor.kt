package com.mohandesomar.nourlv1.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max

object ImageCompressor {

    private const val MAX_DIMENSION = 1024
    private const val JPEG_QUALITY = 80

    suspend fun compressBitmap(original: Bitmap): Pair<Bitmap, String> = withContext(Dispatchers.Default) {
        val width = original.width
        val height = original.height
        val maxSide = max(width, height)

        val scaledBitmap = if (maxSide > MAX_DIMENSION) {
            val scale = MAX_DIMENSION.toFloat() / maxSide
            val matrix = Matrix().apply { postScale(scale, scale) }
            Bitmap.createBitmap(original, 0, 0, width, height, matrix, true)
        } else {
            original
        }

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)

        Pair(scaledBitmap, base64String)
    }

    suspend fun decodeAndCompressUri(context: Context, uri: Uri): Pair<Bitmap, String>? = withContext(Dispatchers.IO) {
        try {
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            val maxSide = max(options.outWidth, options.outHeight)
            var inSampleSize = 1
            while (maxSide / (inSampleSize * 2) >= MAX_DIMENSION) {
                inSampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            inputStream = context.contentResolver.openInputStream(uri)
            val sampledBitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (sampledBitmap != null) {
                compressBitmap(sampledBitmap)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
