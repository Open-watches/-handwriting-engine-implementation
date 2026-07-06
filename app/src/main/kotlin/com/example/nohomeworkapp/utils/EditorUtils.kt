package com.example.nohomeworkapp.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import androidx.compose.ui.graphics.toArgb

object EditorUtils {

    /**
     * Extracts the average color of a sampled grid from the given bitmap region.
     * The region is defined by a normalised [RectF] where coordinates are in [0..1].
     * Samples at most 100 pixels (10 steps per axis) for performance.
     */
    fun extractDominantColor(bitmap: Bitmap, rect: RectF): Int {
        val w = bitmap.width
        val h = bitmap.height

        val left   = (rect.left   * w).toInt().coerceIn(0, w)
        val top    = (rect.top    * h).toInt().coerceIn(0, h)
        val right  = (rect.right  * w).toInt().coerceIn(0, w)
        val bottom = (rect.bottom * h).toInt().coerceIn(0, h)

        if (right <= left || bottom <= top) return Color.WHITE

        // Sampling step: at most 10 steps per dimension
        val stepX = maxOf(1, (right - left) / 10)
        val stepY = maxOf(1, (bottom - top) / 10)

        var r = 0
        var g = 0
        var b = 0
        var count = 0

        for (x in left until right step stepX) {
            for (y in top until bottom step stepY) {
                val pixel = bitmap.getPixel(x, y)
                r += Color.red(pixel)
                g += Color.green(pixel)
                b += Color.blue(pixel)
                count++
            }
        }

        return if (count > 0) Color.rgb(r / count, g / count, b / count) else Color.WHITE
    }

    /**
     * Converts a Compose [Color] to an Android [Color] int (ARGB).
     * Uses the built‑in [toArgb] extension for speed and clarity.
     */
    fun composeColorToInt(color: androidx.compose.ui.graphics.Color): Int {
        return color.toArgb()
    }
}