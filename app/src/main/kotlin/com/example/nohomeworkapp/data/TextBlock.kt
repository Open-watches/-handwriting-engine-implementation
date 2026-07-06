package com.example.nohomeworkapp.data

import android.graphics.RectF

data class TextBlock(
    val text: String,
    val boundingBox: RectF, // normalized coordinates (0..1)
    val confidence: Float = 1f,
    var isSelected: Boolean = false
)