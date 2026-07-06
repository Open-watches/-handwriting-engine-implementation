package com.example.nohomeworkapp.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream

object BitmapUtils {

    fun loadBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val resolver = context.contentResolver
            var bitmap: Bitmap? = null
            resolver.openInputStream(uri)?.use { inputStream ->
                bitmap = BitmapFactory.decodeStream(inputStream)
            }
            // Rotate if needed – this now recycles the original when a rotated copy is created
            bitmap?.let { rotateBitmapIfRequired(context, uri, it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Applies EXIF rotation to the bitmap.
     * If a rotation is necessary, the **original bitmap is recycled** to free memory.
     */
    private fun rotateBitmapIfRequired(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        val orientation = getOrientation(context, uri)
        if (orientation != 0) {
            val matrix = Matrix().apply { postRotate(orientation.toFloat()) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            // Recycle the original bitmap only after a successful rotated copy
            if (rotated != bitmap) {
                bitmap.recycle()
            }
            return rotated
        }
        return bitmap
    }

    /**
     * Reads the EXIF orientation from the image URI.
     * On API 24+ it uses the modern `ExifInterface(uri, context)` constructor;
     * on older devices it falls back to manual stream handling.
     */
    private fun getOrientation(context: Context, uri: Uri): Int {
        return try {
            val exif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ExifInterface(uri, context)
            } else {
                @Suppress("DEPRECATION")
                // Fallback for older APIs – still safe with `use` for auto-close
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    ExifInterface(stream)
                } ?: return 0
            }
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90  -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "edited_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
        }
        return uri
    }
}