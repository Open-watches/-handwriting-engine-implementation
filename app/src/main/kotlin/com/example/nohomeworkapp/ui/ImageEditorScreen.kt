package com.example.nohomeworkapp.ui

import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nohomeworkapp.EditorViewModel
import com.example.nohomeworkapp.data.TextBlock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEditorScreen(
    viewModel: EditorViewModel,
    onPickImage: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deep Text Editor") },
                actions = {
                    Button(onClick = onPickImage) { Text("Pick Image") }
                    Button(
                        onClick = { viewModel.saveImage() },
                        enabled = uiState.workingBitmap != null
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.workingBitmap != null -> {
                    ImageWithOverlay(
                        bitmap = uiState.workingBitmap!!,
                        textBlocks = uiState.textBlocks,
                        selectedIndex = uiState.selectedIndex,
                        onBlockSelected = { index -> viewModel.selectBlock(index) }
                    )
                }
                else -> {
                    Text(
                        text = "No image loaded.\nTap 'Pick Image' to start.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            val selectedIdx = uiState.selectedIndex
            if (selectedIdx != null && uiState.textBlocks.isNotEmpty()) {
                val block = uiState.textBlocks[selectedIdx]
                EditTextDialog(
                    block = block,
                    onDismiss = { viewModel.selectBlock(selectedIdx) },
                    onConfirm = { newText, typeface, color ->
                        viewModel.replaceText(selectedIdx, newText, typeface, color)
                    }
                )
            }

            uiState.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color.Red,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(Color.Black)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun ImageWithOverlay(
    bitmap: android.graphics.Bitmap,
    textBlocks: List<TextBlock>,
    selectedIndex: Int?,
    onBlockSelected: (Int) -> Unit
) {
    val imageBitmap = bitmap.asImageBitmap()
    val textMeasurer = rememberTextMeasurer()

    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.Image(
            bitmap = imageBitmap,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val position = event.changes.firstOrNull()?.position ?: continue
                            val normalizedX = position.x / size.width
                            val normalizedY = position.y / size.height

                            val foundIndex = textBlocks.indexOfFirst { block ->
                                val rect = block.boundingBox
                                normalizedX in rect.left..rect.right &&
                                        normalizedY in rect.top..rect.bottom
                            }
                            if (foundIndex != -1) {
                                onBlockSelected(foundIndex)
                            }
                        }
                    }
                }
        ) {
            textBlocks.forEachIndexed { index, block ->
                val rect = block.boundingBox
                val left = rect.left * size.width
                val top = rect.top * size.height
                val right = rect.right * size.width
                val bottom = rect.bottom * size.height

                val boxColor = if (index == selectedIndex) Color.Red else Color.Green
                drawRect(
                    color = boxColor,
                    topLeft = Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
                    style = Stroke(width = 4f)
                )

                // Compose text drawing – no native Canvas
                val textLayoutResult = textMeasurer.measure(
                    text = block.text,
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.White
                    )
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(left, top - textLayoutResult.size.height)
                )
            }
        }
    }
}

@Composable
fun EditTextDialog(
    block: TextBlock,
    onDismiss: () -> Unit,
    onConfirm: (String, Typeface, Color) -> Unit
) {
    // Pre-select the original foreground colour from the block (if available)
    val initialColor = block.foregroundColor?.let { androidColor ->
        Color(
            red   = android.graphics.Color.red(androidColor)   / 255f,
            green = android.graphics.Color.green(androidColor) / 255f,
            blue  = android.graphics.Color.blue(androidColor)  / 255f
        )
    } ?: Color.White

    var newText by remember { mutableStateOf(block.text) }
    var selectedTypeface by remember { mutableStateOf(Typeface.DEFAULT) }
    var selectedColor by remember { mutableStateOf(initialColor) }

    val fontOptions = listOf(
        "Default" to Typeface.DEFAULT,
        "Serif" to Typeface.SERIF,
        "Sans Serif" to Typeface.SANS_SERIF,
        "Monospace" to Typeface.MONOSPACE
    )
    var expanded by remember { mutableStateOf(false) }

    val colorOptions = listOf(
        Color.White, Color.Black, Color.Red, Color.Green, Color.Blue,
        Color.Yellow, Color.Cyan, Color.Magenta, Color.Gray
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Text") },
        text = {
            Column {
                TextField(
                    value = newText,
                    onValueChange = { newText = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.padding(vertical = 8.dp)) {
                    Button(onClick = { expanded = true }) {
                        Text("Font: ${fontOptions.find { it.second == selectedTypeface }?.first ?: "Default"}")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        fontOptions.forEach { (name, tf) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    selectedTypeface = tf
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Text("Choose Color", fontSize = 14.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    colorOptions.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(color, RoundedCornerShape(4.dp))
                                .border(
                                    width = if (selectedColor == color) 3.dp else 1.dp,
                                    color = if (selectedColor == color) Color.Black else Color.Gray,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            if (event.changes.any { it.pressed }) {
                                                selectedColor = color
                                            }
                                        }
                                    }
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(newText, selectedTypeface, selectedColor) }) {
                Text("Replace")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}