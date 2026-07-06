package com.example.nohomeworkapp.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import kotlin.math.abs

object EditorUtils {

    // Simple dominant colour extraction (average of centre region)
    fun extractDominantColor(bitmap: Bitmap, rect: RectF): Int {
        val w = bitmap.width
        val h = bitmap.height
        val left = (rect.left * w).toInt().coerceAtLeast(0)
        val top = (rect.top * h).toInt().coerceAtLeast(0)
        val right = (rect.right * w).toInt().coerceAtMost(w)
        val bottom = (rect.bottom * h).toInt().coerceAtMost(h)

        if (right <= left || bottom <= top) return Color.WHITE

        // Sample a few central pixels to get background
        var r = 0
        var g = 0
        var b = 0
        var count = 0
        val stepX = maxOf(1, (right - left) / 10)
        val stepY = maxOf(1, (bottom - top) / 10)

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

    // Convert Compose Color to Android Color Int
    fun composeColorToInt(color: androidx.compose.ui.graphics.Color): Int {
        return Color.argb(
            (color.alpha * 255).toInt(),
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
    }
}