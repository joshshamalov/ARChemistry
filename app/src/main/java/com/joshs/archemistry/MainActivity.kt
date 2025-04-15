package com.joshs.archemistry

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavBackStackEntry
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.joshs.archemistry.ui.ARScreen
import com.joshs.archemistry.ui.BackendVisualizationScreen
import com.joshs.archemistry.ui.CustomCropActivity
import com.joshs.archemistry.ui.DebugLogScreen
import com.joshs.archemistry.ui.MainScreen
import com.joshs.archemistry.ui.theme.ARChemistryTheme
import com.joshs.archemistry.utils.Logger
import com.joshs.archemistry.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // OpenCV initialization removed for simplified implementation
        Logger.log("Starting ARChemistry app")

        setContent {
            ARChemistryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToAR = { reactantImage, reagent, product ->
                    navController.navigate("ar/$reactantImage/$reagent/$product")
                },
                onNavigateToBackend = { pipeline ->
                    navController.navigate("backend/$pipeline")
                },
                onNavigateToDebugLogs = {
                    navController.navigate("debug_logs")
                },
                onNavigateToCrop = { imageUri ->
                    // Encode the URI to make it safe for navigation
                    val encodedUri = java.net.URLEncoder.encode(imageUri, "UTF-8")
                    navController.navigate("crop/$encodedUri")
                }
            )
        }

        composable(
            route = "ar/{reactantImage}/{reagent}/{product}"
        ) { backStackEntry: NavBackStackEntry ->
            val reactantImage = backStackEntry.arguments?.getString("reactantImage")
            val reagent = backStackEntry.arguments?.getString("reagent")
            val product = backStackEntry.arguments?.getString("product")

            ARScreen(
                reactantImage = reactantImage,
                reagent = reagent,
                product = product,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "backend/{pipeline}"
        ) { backStackEntry: NavBackStackEntry ->
            val pipeline = backStackEntry.arguments?.getString("pipeline")

            BackendVisualizationScreen(
                pipeline = pipeline,
                onBack = { navController.popBackStack() }
            )
        }

        composable("debug_logs") {
            DebugLogScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("crop/{imageUri}") { backStackEntry: NavBackStackEntry ->
            val encodedImageUriString = backStackEntry.arguments?.getString("imageUri")
            val imageUriString = if (encodedImageUriString != null) {
                java.net.URLDecoder.decode(encodedImageUriString, "UTF-8")
            } else null

            // Create a launcher for the CustomCropActivity
            val cropActivityLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Get the cropped image URI from the result
                    val croppedImageUriString = result.data?.getStringExtra(CustomCropActivity.RESULT_CROPPED_IMAGE_URI)
                    val isOriginal = result.data?.getBooleanExtra(CustomCropActivity.RESULT_IS_ORIGINAL, false) ?: false

                    if (croppedImageUriString != null) {
                        val croppedImageUri = Uri.parse(croppedImageUriString)

                        // Update the selected image with the cropped image
                        // But don't update the original image - it should stay as the first image selected
                        mainViewModel.updateSelectedImage(croppedImageUri, navController.context)

                        // Log the state after cropping
                        Logger.log("After crop - Selected image: $croppedImageUri")
                        Logger.log("After crop - Original image: ${mainViewModel.originalImageUri}")
                        Logger.log("After crop - Is original: $isOriginal")
                    }
                }
                // Navigate back to the main screen
                navController.popBackStack()
            }

            // Launch the CustomCropActivity with the image URI
            LaunchedEffect(imageUriString) {
                if (imageUriString != null) {
                    // Log the current state
                    Logger.log("Loading image: $imageUriString")
                    Logger.log("Original image URI: ${mainViewModel.originalImageUri}")

                    val intent = Intent(navController.context, CustomCropActivity::class.java).apply {
                        putExtra(CustomCropActivity.EXTRA_IMAGE_URI, imageUriString)

                        // Always pass the original image URI if available
                        if (mainViewModel.originalImageUri != null) {
                            putExtra(CustomCropActivity.EXTRA_ORIGINAL_IMAGE_URI, mainViewModel.originalImageUri.toString())
                            Logger.log("Passing original image URI: ${mainViewModel.originalImageUri}")
                        }
                    }
                    cropActivityLauncher.launch(intent)
                } else {
                    // If no image URI is provided, go back
                    navController.popBackStack()
                }
            }
        }
    }
}