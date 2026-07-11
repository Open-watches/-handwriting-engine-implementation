package com.example.nohomeworkapp.data

import android.graphics.RectF

data class TextBlock(
    val text: String,
    val boundingBox: RectF,           // normalized coordinates (0..1)
    val confidence: Float = 1f,
    var isSelected: Boolean = false,
    val backgroundColor: Int? = null, // cached background colour for fast erasing
    val foregroundColor: Int? = null, // original ink colour (Android ARGB int)
    val language: String? = null      // e.g. "my", "en", "ja", null if unknown
)