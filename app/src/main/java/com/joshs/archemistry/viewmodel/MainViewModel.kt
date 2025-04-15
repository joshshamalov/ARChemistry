package com.joshs.archemistry.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshs.archemistry.reaction.Reagent
import com.joshs.archemistry.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the main screen to persist state across navigation events.
 */
class MainViewModel : ViewModel() {
    // Image state
    var selectedImageUri by mutableStateOf<Uri?>(null)
    var selectedImageBitmap by mutableStateOf<Bitmap?>(null)

    // Original image state (before cropping)
    var originalImageUri by mutableStateOf<Uri?>(null)
    var originalImageBitmap by mutableStateOf<Bitmap?>(null)

    // Navigation state
    var showCropScreen by mutableStateOf(false)

    // Reagent state
    var selectedReagent by mutableStateOf<Reagent?>(null)

    // Function to update the selected image
    fun updateSelectedImage(uri: Uri?, bitmap: Bitmap?) {
        // If this is the first image being set, also set it as the original
        if (selectedImageUri == null && originalImageUri == null && uri != null) {
            originalImageUri = uri
            originalImageBitmap = bitmap
        }

        selectedImageUri = uri
        selectedImageBitmap = bitmap
    }

    // Function to update the selected image and load the bitmap if needed
    fun updateSelectedImage(uri: Uri?, context: Context) {
        // If this is the first image being set, also set it as the original
        val isFirstImage = selectedImageUri == null && originalImageUri == null && uri != null

        selectedImageUri = uri

        // Load the bitmap from the URI if it's not null
        if (uri != null) {
            viewModelScope.launch {
                try {
                    val bitmap = loadBitmapFromUri(context, uri)
                    selectedImageBitmap = bitmap

                    // If this is the first image, also set it as the original
                    if (isFirstImage) {
                        originalImageUri = uri
                        originalImageBitmap = bitmap
                    }
                } catch (e: Exception) {
                    Logger.error("Error loading bitmap from URI: ${e.message}", e)
                }
            }
        } else {
            selectedImageBitmap = null
        }
    }

    // Helper function to load a bitmap from a URI
    private suspend fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                return@withContext BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            Logger.error("Error loading bitmap from URI: ${e.message}", e)
        }
        return@withContext null
    }

    // Function to set the original image
    fun setOriginalImage(uri: Uri?, bitmap: Bitmap?) {
        originalImageUri = uri
        originalImageBitmap = bitmap
    }

    // Function to update the selected reagent
    fun updateSelectedReagent(reagent: Reagent?) {
        selectedReagent = reagent
    }

    // Function to clear the selected image but keep the original
    fun clearSelectedImage() {
        selectedImageUri = null
        selectedImageBitmap = null
    }

    // Function to clear all image data
    fun clearAllImageData() {
        selectedImageUri = null
        selectedImageBitmap = null
        originalImageUri = null
        originalImageBitmap = null
    }
}
