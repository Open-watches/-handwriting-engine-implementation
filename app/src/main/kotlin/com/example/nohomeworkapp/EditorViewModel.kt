package com.example.nohomeworkapp

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nohomeworkapp.data.TextBlock
import com.example.nohomeworkapp.ocr.TextRecognizer
import com.example.nohomeworkapp.utils.BitmapUtils
import com.example.nohomeworkapp.utils.EditorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val recognizer = TextRecognizer()

    // State
    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    fun onImagePicked(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val bmp = BitmapUtils.loadBitmap(getApplication(), uri)
            if (bmp == null) {
                _uiState.update { it.copy(errorMessage = "Failed to load image") }
                return@launch
            }
            val blocks = recognizer.detectText(bmp)
            _uiState.update {
                it.copy(
                    originalBitmap = bmp,
                    workingBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true),
                    textBlocks = blocks,
                    selectedIndex = null,
                    isLoading = false,
                    errorMessage = null
                )
            }
        }
    }

    fun selectBlock(index: Int) {
        _uiState.update { state ->
            val newBlocks = state.textBlocks.mapIndexed { i, block ->
                block.copy(isSelected = i == index)
            }
            state.copy(
                textBlocks = newBlocks,
                selectedIndex = if (state.selectedIndex == index) null else index
            )
        }
    }

    fun replaceText(
        index: Int,
        newText: String,
        typeface: Typeface,
        color: androidx.compose.ui.graphics.Color
    ) {
        val state = _uiState.value
        val block = state.textBlocks.getOrNull(index) ?: return
        val workingBmp = state.workingBitmap ?: return

        val mutableBmp = workingBmp.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBmp)

        val rect = block.boundingBox
        val w = mutableBmp.width
        val h = mutableBmp.height
        val left = (rect.left * w).toInt()
        val top = (rect.top * h).toInt()
        val right = (rect.right * w).toInt()
        val bottom = (rect.bottom * h).toInt()

        // 1. Fill background (erase old text)
        val bgColor = EditorUtils.extractDominantColor(state.originalBitmap ?: mutableBmp, rect)
        val bgPaint = Paint().apply {
            this.color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), bgPaint)

        // 2. Draw new text
        val boxWidth = right - left
        val boxHeight = bottom - top
        val textSize = boxHeight * 0.6f // roughly fit vertically

        val textPaint = Paint().apply {
            this.color = EditorUtils.composeColorToInt(color)
            this.textSize = textSize
            this.typeface = typeface
            this.textAlign = Paint.Align.CENTER
            this.isAntiAlias = true
        }

        // Center the text
        val x = (left + right) / 2f
        val y = (top + bottom) / 2f - (textPaint.descent() + textPaint.ascent()) / 2f

        // Scale down if text is too wide
        var finalText = newText
        var currentSize = textSize
        while (currentSize > 10) {
            textPaint.textSize = currentSize
            val textWidth = textPaint.measureText(finalText)
            if (textWidth < boxWidth * 0.9) break
            currentSize *= 0.9f
        }
        textPaint.textSize = currentSize
        canvas.drawText(finalText, x, y, textPaint)

        // Update state
        _uiState.update {
            it.copy(
                workingBitmap = mutableBmp,
                textBlocks = it.textBlocks.mapIndexed { i, b ->
                    if (i == index) b.copy(text = newText) else b
                },
                selectedIndex = null
            )
        }
    }

    fun saveImage(): Uri? {
        val bmp = _uiState.value.workingBitmap ?: return null
        return BitmapUtils.saveBitmapToGallery(getApplication(), bmp)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

data class EditorUiState(
    val originalBitmap: Bitmap? = null,
    val workingBitmap: Bitmap? = null,
    val textBlocks: List<TextBlock> = emptyList(),
    val selectedIndex: Int? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)