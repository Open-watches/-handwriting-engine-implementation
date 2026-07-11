package com.example.nohomeworkapp

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.ui.graphics.Color as ComposeColor
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

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    fun onImagePicked(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val bmp = BitmapUtils.loadBitmap(getApplication(), uri)
            if (bmp == null) {
                _uiState.update { it.copy(errorMessage = "Failed to load image") }
                return@launch
            }
            val original = bmp
            val working = original.copy(Bitmap.Config.ARGB_8888, true)

            // 1. OCR with language detection
            val rawBlocks = recognizer.detectText(original)

            // 2. Extract background and foreground colours from the original image
            val blocksWithStyle = rawBlocks.map { block ->
                val bgColor = EditorUtils.extractDominantColor(original, block.boundingBox)
                val fgColor = EditorUtils.extractForegroundColor(original, block.boundingBox, bgColor)
                block.copy(
                    backgroundColor = bgColor,
                    foregroundColor = fgColor
                )
            }

            _uiState.update {
                it.copy(
                    originalBitmap = original,
                    workingBitmap = working,
                    textBlocks = blocksWithStyle,
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
        color: ComposeColor
    ) {
        val state = _uiState.value
        val block = state.textBlocks.getOrNull(index) ?: return
        val workingBmp = state.workingBitmap ?: return
        val originalBmp = state.originalBitmap ?: workingBmp

        viewModelScope.launch(Dispatchers.Default) {
            val canvas = Canvas(workingBmp)

            val rect = block.boundingBox
            val w = workingBmp.width
            val h = workingBmp.height
            val left   = (rect.left   * w).toInt()
            val top    = (rect.top    * h).toInt()
            val right  = (rect.right  * w).toInt()
            val bottom = (rect.bottom * h).toInt()

            // 1. Erase old text using cached background colour
            val bgColor = block.backgroundColor
                ?: EditorUtils.extractDominantColor(originalBmp, rect)
            bgPaint.color = bgColor
            canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), bgPaint)

            // 2. Draw new text
            val boxWidth = right - left
            val boxHeight = bottom - top
            var textSize = boxHeight * 0.6f

            textPaint.apply {
                this.color = EditorUtils.composeColorToInt(color)
                this.typeface = typeface
                this.textSize = textSize
            }

            val x = (left + right) / 2f
            val y = (top + bottom) / 2f - (textPaint.descent() + textPaint.ascent()) / 2f

            var currentSize = textSize
            while (currentSize > 10f) {
                textPaint.textSize = currentSize
                val textWidth = textPaint.measureText(newText)
                if (textWidth < boxWidth * 0.9f) break
                currentSize *= 0.9f
            }
            textPaint.textSize = currentSize
            canvas.drawText(newText, x, y, textPaint)

            _uiState.update {
                it.copy(
                    textBlocks = it.textBlocks.mapIndexed { i, b ->
                        if (i == index) b.copy(text = newText) else b
                    },
                    selectedIndex = null
                )
            }
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