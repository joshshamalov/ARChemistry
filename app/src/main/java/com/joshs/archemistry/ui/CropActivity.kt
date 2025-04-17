package com.joshs.archemistry.ui

import android.content.Intent
import android.graphics.Color
import com.joshs.archemistry.R
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView.Guidelines
import com.joshs.archemistry.utils.Logger

/**
 * Activity that handles image cropping using the Android-Image-Cropper library.
 * This provides a fully interactive crop view with draggable handles.
 */
class CropActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val RESULT_CROPPED_IMAGE_URI = "result_cropped_image_uri"
    }

    // Register for the crop image activity result
    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Get the cropped image URI
            val croppedImageUri = result.uriContent

            // Return the cropped image URI to the calling activity
            val resultIntent = Intent().apply {
                putExtra(RESULT_CROPPED_IMAGE_URI, croppedImageUri.toString())
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        } else {
            // Handle crop failure
            Logger.error("Image cropping failed: ${result.error?.message}")
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // We don't want immersive mode for the crop activity as it hides the toolbar
        // Keep the status bar and navigation visible for better UX with the toolbar

        // Get the image URI from the intent
        val imageUriString = intent.getStringExtra(EXTRA_IMAGE_URI)
        if (imageUriString == null) {
            Logger.error("No image URI provided to CropActivity")
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        val imageUri = Uri.parse(imageUriString)

        // Launch the crop activity with the image URI
        cropImage.launch(
            CropImageContractOptions(
                uri = imageUri,
                cropImageOptions = CropImageOptions().apply {
                    guidelines = Guidelines.ON  // Always show guidelines
                    fixAspectRatio = false  // Allow free-form cropping
                    autoZoomEnabled = true
                    showCropOverlay = true
                    showProgressBar = true

                    // Enable image flipping and rotation
                    allowFlipping = true
                    allowRotation = true

                    // Show the toolbar with crop, rotate, and flip buttons
                    cropMenuCropButtonTitle = "Crop"
                    activityTitle = "Crop Image"
                    activityMenuIconColor = android.graphics.Color.WHITE
                    toolbarColor = android.graphics.Color.parseColor("#121212") // ARDarkBackground
                    toolbarTitleColor = android.graphics.Color.WHITE

                    // Show menu options for rotation and flipping
                    cropMenuCropButtonIcon = android.R.drawable.ic_menu_crop

                    // Basic customization
                    backgroundColor = android.graphics.Color.parseColor("#99121212")  // Semi-transparent dark background
                }
            )
        )
    }
}
