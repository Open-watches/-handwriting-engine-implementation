package com.example.nohomeworkapp.ocr

import android.graphics.Bitmap
import android.graphics.RectF
import com.example.nohomeworkapp.data.TextBlock
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions   // current Latin-only dependency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TextRecognizer {

    // Using Latin-only default options – this compiles with your current dependencies.
    // To add Burmese and other scripts, replace the dependency in build.gradle:
    //   implementation 'com.google.mlkit:text-recognition:16.0.0'
    // Then replace this recognizer with:
    //   private val recognizer = TextRecognition.getClient(
    //       TextRecognizerOptions.Builder()
    //           .setScripts(Script.LATIN, Script.MYANMAR)
    //           .build()
    //   )
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun detectText(bitmap: Bitmap): List<TextBlock> = withContext(Dispatchers.IO) {
        val image = InputImage.fromBitmap(bitmap, 0)

        return@withContext try {
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