package com.joshs.archemistry.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joshs.archemistry.ui.theme.*
import com.joshs.archemistry.utils.Logger
import com.joshs.archemistry.viewmodel.MainViewModel

/**
 * Screen that visualizes the backend processing pipeline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackendVisualizationScreen(
    @Suppress("UNUSED_PARAMETER") pipeline: String?,
    viewModel: MainViewModel,  // Accept the ViewModel parameter
    onBack: () -> Unit
) {
    // Use the passed MainViewModel to get the selected image
    val selectedImageBitmap = viewModel.selectedImageBitmap
    val selectedReagent = viewModel.selectedReagent
    val selectedImageUri = viewModel.selectedImageUri

    // Log that we're using the passed ViewModel
    Logger.log("Using passed ViewModel with image: ${selectedImageBitmap != null}")

    // Log the current state
    val context = LocalContext.current
    Logger.log("Backend visualization opened with image: ${selectedImageBitmap != null}")
    Logger.log("Backend visualization image URI: $selectedImageUri")

    if (selectedImageBitmap != null) {
        Logger.log("Backend visualization image dimensions: ${selectedImageBitmap.width}x${selectedImageBitmap.height}")
    } else {
        Logger.log("Backend visualization image bitmap is null")
        // Try to load the image from the URI if available
        if (selectedImageUri != null) {
            Logger.log("Attempting to load image from URI: $selectedImageUri")
            // Try to load the image from the URI
            viewModel.updateSelectedImage(selectedImageUri, context)
        }
    }

    if (selectedReagent != null) {
        Logger.log("Selected reagent: ${selectedReagent.name}")
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backend Visualization") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Processing Pipeline",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Step 1: Image Input
            PipelineStep(
                stepNumber = 1,
                title = "Image Input & Reagent Selection",
                description = "The user selects or captures a reaction image. They are prompted to crop and optionally rotate the image to focus only on the reactant. A reagent is selected from the dropdown.",
                imageBitmap = selectedImageBitmap,
                reagentName = selectedReagent?.name
            )

            // Step 2: Image Preprocessing
            PipelineStep(
                stepNumber = 2,
                title = "Image Preprocessing",
                description = "The selected image is processed by OpenCV: converted to grayscale, denoised, and enhanced to prepare for text and structure recognition."
            )

            // Step 3: Text Recognition
            PipelineStep(
                stepNumber = 3,
                title = "Text Recognition / Bounding Boxes",
                description = "ML Kit scans the image for chemical symbols. For each symbol detected, a blue bounding box is drawn. Bounding box coordinates and text labels are saved for the next masking step."
            )

            // Step 4: Text Masking
            PipelineStep(
                stepNumber = 4,
                title = "Atomic Symbol Text Masking",
                description = "Text within the blue bounding boxes is masked out using OpenCV, but the boxes remain as placeholders for those atom locations."
            )

            // Step 5: Line-Angle Structure Recognition
            PipelineStep(
                stepNumber = 5,
                title = "Line-Angle Structure Recognition",
                description = "OpenCV detects bond lines in the masked image. Green lines represent detected bonds. Red circles are drawn at junctions and endpoints (representing implicit Carbon atoms). Coordinates of all vertices and lines are recorded."
            )

            // Step 6: Reactant Graph Construction
            PipelineStep(
                stepNumber = 6,
                title = "Reactant Graph Construction",
                description = "An undirected multigraph is created using JGraphT. Nodes represent atoms (explicit or implicit), and edges represent bonds. This graph is preserved as the original reactant graph."
            )

            // Step 7: Reaction Execution
            PipelineStep(
                stepNumber = 7,
                title = "Reaction Execution",
                description = "A copy of the multigraph is passed into the reaction engine. The selected reagent's logic is applied, and the structure is modified accordingly."
            )

            // Step 8: Coordinate Generation
            PipelineStep(
                stepNumber = 8,
                title = "Coordinate Generation",
                description = "A custom coordinate generator is used to convert the 2D graph into a 3D coordinate model."
            )

            // Step 9: AR Rendering
            PipelineStep(
                stepNumber = 9,
                title = "AR Rendering",
                description = "A simplified AR view is used to render the final molecule."
            )
        }
    }
}

@Composable
fun PipelineStep(
    stepNumber: Int,
    title: String,
    description: String,
    imageBitmap: Bitmap? = null,
    reagentName: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Step number and title
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Step $stepNumber",
                    color = ARTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Description
            Text(
                text = description,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Justify
            )

            // Show reagent name if available (only for Step 1)
            if (stepNumber == 1 && reagentName != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Selected Reagent: $reagentName",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ARPrimaryBlue
                )
            }

            // Visualization area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp) // Increased height for better image display
                    .padding(top = 16.dp)
                    .background(ARCardBackground, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    // For Step 1 with image, show the actual image
                    stepNumber == 1 && imageBitmap != null -> {
                        Logger.log("Displaying image for Step 1 with dimensions: ${imageBitmap.width}x${imageBitmap.height}")
                        // Convert bitmap to ImageBitmap outside of the composable
                        val imageBitmapState = remember(imageBitmap) { imageBitmap.asImageBitmap() }
                        Image(
                            bitmap = imageBitmapState,
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    // For other steps or if no image, show placeholder
                    else -> {
                        if (stepNumber == 1) {
                            Logger.log("No image available for Step 1")
                        }
                        Text(
                            text = "Visualization placeholder for Step $stepNumber",
                            color = ARTextSecondary
                        )
                    }
                }
            }
        }
    }
}
