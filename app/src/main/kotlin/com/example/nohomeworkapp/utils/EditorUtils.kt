package com.example.nohomeworkapp.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import androidx.compose.ui.graphics.toArgb

object EditorUtils {

    fun extractDominantColor(bitmap: Bitmap, rect: RectF): Int {
        val w = bitmap.width
        val h = bitmap.height
        val left   = (rect.left   * w).toInt().coerceIn(0, w)
        val top    = (rect.top    * h).toInt().coerceIn(0, h)
        val right  = (rect.right  * w).toInt().coerceIn(0, w)
        val bottom = (rect.bottom * h).toInt().coerceIn(0, h)

        if (right <= left || bottom <= top) return Color.WHITE

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
     * Finds the most frequent colour in the region that is **not** close to [bgColor].
     * This extracts the original ink/foreground colour.
     */
    fun extractForegroundColor(bitmap: Bitmap, rect: RectF, bgColor: Int): Int {
        val w = bitmap.width
        val h = bitmap.height
        val left   = (rect.left   * w).toInt().coerceIn(0, w)
        val top    = (rect.top    * h).toInt().coerceIn(0, h)
        val right  = (rect.right  * w).toInt().coerceIn(0, w)
        val bottom = (rect.bottom * h).toInt().coerceIn(0, h)

        if (right <= left || bottom <= top) return Color.BLACK

        val colorCount = mutableMapOf<Int, Int>()
        // Sample every 2 pixels for performance
        for (x in left until right step 2) {
            for (y in top until bottom step 2) {
                val pixel = bitmap.getPixel(x, y)
                if (colorDistance(pixel, bgColor) > 50) {
                    colorCount[pixel] = (colorCount[pixel] ?: 0) + 1
                }
            }
        }
        return colorCount.maxByOrNull { it.value }?.key ?: Color.BLACK
    }

    private fun colorDistance(c1: Int, c2: Int): Int {
        return kotlin.math.abs(Color.red(c1)   - Color.red(c2)) +
               kotlin.math.abs(Color.green(c1) - Color.green(c2)) +
               kotlin.math.abs(Color.blue(c1)  - Color.blue(c2))
    }

    fun composeColorToInt(color: androidx.compose.ui.graphics.Color): Int = color.toArgb()
}