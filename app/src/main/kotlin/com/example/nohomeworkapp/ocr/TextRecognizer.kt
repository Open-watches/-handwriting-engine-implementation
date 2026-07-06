package com.example.nohomeworkapp.ocr

import android.graphics.Bitmap
import android.graphics.RectF
import com.example.nohomeworkapp.data.TextBlock
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import com.google.mlkit.vision.text.Script
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TextRecognizer {

    // Build recognizer with explicit script support.
    // Add/remove scripts as needed. Latin + Myanmar cover most use cases.
    private val recognizer = TextRecognition.getClient(
        TextRecognizerOptions.Builder()
            .setScripts(Script.LATIN, Script.MYANMAR)   // Burmese support
            // You can add more: Script.DEVANAGARI, Script.CJK, Script.ARABIC, etc.
            .build()
    )

    suspend fun detectText(bitmap: Bitmap): List<TextBlock> = withContext(Dispatchers.IO) {
        val image = InputImage.fromBitmap(bitmap, 0)

        return@withContext try {
            // Uses kotlinx.coroutines.tasks.await() for proper cancellation
            val result = recognizer.process(image).await()

            result.textBlocks.mapNotNull { block ->
                val rect = block.boundingBox ?: return@mapNotNull null
                TextBlock(
                    text = block.text,
                    boundingBox = RectF(
                        rect.left.toFloat() / bitmap.width,
                        rect.top.toFloat() / bitmap.height,
                        rect.right.toFloat() / bitmap.width,
                        rect.bottom.toFloat() / bitmap.height
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}