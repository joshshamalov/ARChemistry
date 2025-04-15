package com.joshs.archemistry.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Screen that visualizes the backend processing pipeline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackendVisualizationScreen(
    @Suppress("UNUSED_PARAMETER") pipeline: String?,
    onBack: () -> Unit
) {
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
                description = "The user selects or captures a reaction image. They are prompted to crop and optionally rotate the image to focus only on the reactant. A reagent is selected from the dropdown."
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
    description: String
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
                    color = Color.White,
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
            
            // Placeholder for image (in a real app, this would show actual processing results)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(top = 16.dp)
                    .background(Color.LightGray, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Visualization placeholder for Step $stepNumber",
                    color = Color.DarkGray
                )
            }
        }
    }
}
