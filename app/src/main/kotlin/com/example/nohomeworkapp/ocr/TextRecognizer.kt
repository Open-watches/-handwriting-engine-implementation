package com.example.nohomeworkapp.ocr

import android.graphics.Bitmap
import android.graphics.RectF
import com.example.nohomeworkapp.data.TextBlock
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TextRecognizer {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun detectText(bitmap: Bitmap): List<TextBlock> = withContext(Dispatchers.IO) {
        val image = InputImage.fromBitmap(bitmap, 0)
        return@withContext try {
            val result = recognizer.process(image).await()
            result.textBlocks.map { block ->
                val rect = block.boundingBox ?: return@map null
                TextBlock(
                    text = block.text,
                    boundingBox = RectF(
                        rect.left.toFloat() / bitmap.width,
                        rect.top.toFloat() / bitmap.height,
                        rect.right.toFloat() / bitmap.width,
                        rect.bottom.toFloat() / bitmap.height
                    )
                )
            }.filterNotNull()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

// Extension to convert ML Kit Task to coroutine
suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    }