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

    private val recognizer = TextRecognition.getClient(
        TextRecognizerOptions.Builder()
            .setScripts(
                Script.LATIN,
                Script.MYANMAR,        // Burmese
                Script.CJK,            // Chinese, Japanese, Korean
                Script.DEVANAGARI,     // Hindi, etc.
                Script.ARABIC,
                Script.THAI
                // Add more as needed – full list in Google documentation
            )
            .build()
    )

    suspend fun detectText(bitmap: Bitmap): List<TextBlock> = withContext(Dispatchers.IO) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = recognizer.process(image).await()

        result.textBlocks.mapNotNull { block ->
            val rect = block.boundingBox ?: return@mapNotNull null
            val language = block.recognizedLanguage?.languageTag // e.g. "my", "en"
            TextBlock(
                text = block.text,
                boundingBox = RectF(
                    rect.left.toFloat() / bitmap.width,
                    rect.top.toFloat() / bitmap.height,
                    rect.right.toFloat() / bitmap.width,
                    rect.bottom.toFloat() / bitmap.height
                ),
                language = language
            )
        }
    }
}