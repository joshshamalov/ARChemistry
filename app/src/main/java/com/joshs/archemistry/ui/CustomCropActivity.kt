package com.joshs.archemistry.ui

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.canhub.cropper.CropImageView
import com.joshs.archemistry.R
import com.joshs.archemistry.utils.Logger

/**
 * Custom crop activity with a scaled-down image view, reset button, and bottom crop button.
 * This provides a fully interactive crop view with draggable handles.
 */
class CustomCropActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_ORIGINAL_IMAGE_URI = "extra_original_image_uri"
        const val RESULT_CROPPED_IMAGE_URI = "result_cropped_image_uri"
        const val RESULT_IS_ORIGINAL = "result_is_original"
    }

    private lateinit var cropImageView: CropImageView
    private var currentUri: Uri? = null
    private var originalUri: Uri? = null
    private var isOriginalImage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the window background to match our theme
        window.setBackgroundDrawableResource(R.color.ar_dark_background)

        setContentView(R.layout.activity_custom_crop)

        // Make sure the status bar height is properly accounted for
        val statusBarSpacer = findViewById<View>(R.id.statusBarSpacer)
        val statusBarHeight = getStatusBarHeight()
        val params = statusBarSpacer.layoutParams
        params.height = statusBarHeight
        statusBarSpacer.layoutParams = params

        // Get the image URIs from the intent
        val imageUriString = intent.getStringExtra(EXTRA_IMAGE_URI)
        val originalImageUriString = intent.getStringExtra(EXTRA_ORIGINAL_IMAGE_URI)

        if (imageUriString == null) {
            Logger.error("No image URI provided to CustomCropActivity")
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        currentUri = Uri.parse(imageUriString)
        originalUri = if (originalImageUriString != null) {
            Uri.parse(originalImageUriString)
        } else {
            currentUri // If no original URI is provided, use the current URI
        }

        // Check if we're already using the original image
        isOriginalImage = currentUri.toString() == originalUri.toString()

        Logger.log("CustomCropActivity - Current URI: $currentUri")
        Logger.log("CustomCropActivity - Original URI: $originalUri")
        Logger.log("CustomCropActivity - Is Original Image: $isOriginalImage")

        // Update the reset button text based on whether we're using the original image
        updateResetButtonText()

        // Set up the crop image view
        cropImageView = findViewById(R.id.cropImageView)

        // Configure the crop image view programmatically to use full image
        // We'll set the crop rect to null after the image is loaded

        // Set the image URI
        cropImageView.setImageUriAsync(currentUri)

        // Set the crop window to the full image size
        cropImageView.setOnSetImageUriCompleteListener { view, uri, error ->
            if (error == null) {
                // Reset the crop window to the full image size
                view.cropRect = view.wholeImageRect
            }
        }

        // Set up the crop image complete listener
        cropImageView.setOnCropImageCompleteListener { view, result ->
            if (result.isSuccessful) {
                // Return the cropped image URI to the calling activity
                val resultIntent = Intent().apply {
                    putExtra(RESULT_CROPPED_IMAGE_URI, result.uriContent.toString())
                    putExtra(RESULT_IS_ORIGINAL, isOriginalImage)
                }

                Logger.log("Crop complete - Returning URI: ${result.uriContent}")
                Logger.log("Crop complete - Is Original: $isOriginalImage")

                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                // Handle crop failure
                Logger.error("Image cropping failed: ${result.error?.message}")
                setResult(RESULT_CANCELED)
                finish()
            }
        }

        // Set up the reset button
        findViewById<View>(R.id.resetButton).setOnClickListener {
            // Reset the crop window and zoom
            cropImageView.clearImage()

            // Always restore the original image if available
            if (originalUri != null) {
                // Load the original image
                cropImageView.setImageUriAsync(originalUri)
                currentUri = originalUri
                isOriginalImage = true
                Logger.log("Restored original image: $originalUri")
            } else {
                // Just reload the current image
                cropImageView.setImageUriAsync(currentUri)
                Logger.log("No original image available, reloaded current: $currentUri")
            }

            // The setOnSetImageUriCompleteListener will handle setting the crop window to the full image
        }

        // Set up the crop button
        findViewById<View>(R.id.cropButton).setOnClickListener {
            // Crop the image
            cropImageView.croppedImageAsync()
        }

        // Set up rotation buttons
        findViewById<View>(R.id.rotateLeftButton).setOnClickListener {
            cropImageView.rotateImage(-90)
        }

        findViewById<View>(R.id.rotateRightButton).setOnClickListener {
            cropImageView.rotateImage(90)
        }

        findViewById<View>(R.id.flipButton).setOnClickListener {
            cropImageView.flipImageHorizontally()
        }
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 24
    }

    private fun updateResetButtonText() {
        val resetButton = findViewById<android.widget.Button>(R.id.resetButton)
        if (!isOriginalImage && originalUri != null && currentUri != originalUri) {
            resetButton.text = "RESTORE ORIGINAL"
        } else {
            resetButton.text = "RESET"
        }
    }
}
