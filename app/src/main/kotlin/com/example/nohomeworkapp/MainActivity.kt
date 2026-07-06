package com.example.nohomeworkapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nohomeworkapp.ui.ImageEditorScreen
import com.example.nohomeworkapp.ui.theme.NoHomeworkAppTheme

class MainActivity : ComponentActivity() {

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onImagePicked(it)
        }
    }

    private val viewModel by lazy {
        EditorViewModel(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoHomeworkAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ImageEditorScreen(
                        viewModel = viewModel,
                        onPickImage = { imagePickerLauncher.launch("image/*") }
                    )
                }
            }
        }
    }
}