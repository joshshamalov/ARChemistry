package com.joshs.archemistry.feature_reactant

import android.Manifest
import android.graphics.Color // Import Color
import android.content.Context // Import Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider // Import FileProvider
import java.io.File // Import File
import java.text.SimpleDateFormat // Import SimpleDateFormat
import java.util.Date // Import Date
import java.util.Locale // Import Locale
import java.util.Objects // Import Objects
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.accompanist.permissions.* // Import Accompanist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Helper function to get storage permission based on Android version
private fun getStoragePermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class) // Add ExperimentalPermissionsApi
@Composable
fun ReactantScreen(
    viewModel: ReactantViewModel = viewModel(),
    // Removed unused navigation callbacks
    onNavigateToArProduct: (encodedModelDataJson: String) -> Unit // Expects the encoded JSON string
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // --- Handle Navigation Events ---
    LaunchedEffect(key1 = true) { // Use key1 = true to run once and keep collecting
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.NavigateToArProduct -> {
                    // TODO: Need to URL-encode the JSON string before passing as nav argument
                    val encodedJson = java.net.URLEncoder.encode(event.modelDataJson, "UTF-8")
                    onNavigateToArProduct(encodedJson)
                }
                // Handle other potential navigation events here
            }
        }
    }

    // --- Permissions Handling ---
    val storagePermission = getStoragePermission()
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            storagePermission,
            Manifest.permission.CAMERA
        )
    )
    // Keep track if permission rationale should be shown
    var showRationaleDialog by remember { mutableStateOf(false) }

    // --- Activity Result Launchers ---
    @Suppress("DEPRECATION") // Suppress warning for deprecated contract
    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            viewModel.onImageCropped(result.uriContent)
        } else {
            viewModel.onImageCropped(null)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            @Suppress("DEPRECATION") // Suppress warning for deprecated options
            val cropOptions = CropImageContractOptions(it, CropImageOptions().apply {
                // Customize colors
                activityBackgroundColor = Color.DKGRAY // Gray background
                guidelinesColor = Color.GREEN         // Green guidelines
                borderCornerColor = Color.GREEN       // Green corners
                // Add other customizations here if needed
            })
            imageCropLauncher.launch(cropOptions)
        }
    }

    // TODO: Implement proper URI provider for camera capture
    // Placeholder for camera URI - THIS NEEDS A REAL IMPLEMENTATION WITH FILEPROVIDER
    var tempCameraUri: Uri? by remember { mutableStateOf(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success && tempCameraUri != null) {
            @Suppress("DEPRECATION") // Suppress warning for deprecated options
            val cropOptions = CropImageContractOptions(tempCameraUri!!, CropImageOptions().apply {
                 // Customize colors
                activityBackgroundColor = Color.DKGRAY // Gray background
                guidelinesColor = Color.GREEN         // Green guidelines
                borderCornerColor = Color.GREEN       // Green corners
                // Add other customizations here if needed
            })
            imageCropLauncher.launch(cropOptions)
        } else {
             println("Camera failed or URI was null")
             // Handle failure - maybe show a message
        }
    }

    // --- UI ---
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("ARChemistry") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Image Preview Area (Unchanged)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.croppedImageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(uiState.croppedImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Selected chemical structure",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = "No image selected\nCapture or select an image of a chemical reaction",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Image Input Buttons - Adjusted Row for consistent height
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), // Ensure buttons fill height
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Capture Image Button with Permission Check
                Button(
                    onClick = {
                        val cameraPermission = permissionsState.permissions.find { it.permission == Manifest.permission.CAMERA }
                        if (cameraPermission?.status?.isGranted == true) {
                            // TODO: Replace placeholder URI creation with FileProvider
                            tempCameraUri = ComposeFileProvider.getImageUri(context) // Needs implementation
                            val uriToLaunch = tempCameraUri // Capture the value locally
                            if(uriToLaunch != null) {
                                cameraLauncher.launch(uriToLaunch) // Use the local variable
                            } else {
                                println("Error creating camera URI")
                                // Show error message
                            }
                        } else {
                            // Request permission or show rationale
                            if (cameraPermission?.status?.shouldShowRationale == true) {
                                showRationaleDialog = true // Trigger rationale dialog if needed
                            } else {
                                permissionsState.launchMultiplePermissionRequest()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight() // Fill height within Row
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Capture Image")
                }

                // Select from Gallery Button with Permission Check
                Button(
                    onClick = {
                        val storagePerm = permissionsState.permissions.find { it.permission == storagePermission }
                        if (storagePerm?.status?.isGranted == true) {
                            imagePickerLauncher.launch("image/*")
                        } else {
                             if (storagePerm?.status?.shouldShowRationale == true) {
                                showRationaleDialog = true // Trigger rationale dialog if needed
                            } else {
                                permissionsState.launchMultiplePermissionRequest()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight() // Fill height within Row
                ) {
                     Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                     Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Select from Gallery")
                }
            }

            // Reagent Selection (Unchanged)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) // Use HorizontalDivider
            Text("Select Reagent:", style = MaterialTheme.typography.titleMedium)
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.selectedReagent ?: "Select a reagent",
                    onValueChange = {}, readOnly = true, label = { Text("Reagent") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    uiState.availableReagents.forEach { reagent ->
                        DropdownMenuItem(
                            text = { Text(reagent) },
                            onClick = {
                                viewModel.onReagentSelected(reagent)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Process Button (Unchanged)
            // Get content resolver from context
            val contentResolver = LocalContext.current.contentResolver
            Button(
                // Pass content resolver to the ViewModel function
                onClick = { viewModel.onProcessReactionClicked(contentResolver) },
                enabled = uiState.isProcessButtonEnabled && !uiState.isProcessing,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Processing...")
                } else {
                    Text("Process Reaction")
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Push bottom buttons down

            // Navigation Buttons (Unchanged)
            // Removed unused navigation buttons

            // Error Display (Unchanged)
            uiState.errorMessage?.let { error ->
                Text(text = "Error: $error", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            }

            // --- Permission Rationale Dialog ---
            if (showRationaleDialog) {
                AlertDialog(
                    onDismissRequest = { showRationaleDialog = false },
                    title = { Text("Permissions Required") },
                    text = { Text("This app needs Camera and Storage access to capture or select chemical images. Please grant the permissions.") },
                    confirmButton = {
                        Button(onClick = {
                            showRationaleDialog = false
                            permissionsState.launchMultiplePermissionRequest()
                        }) { Text("Grant Permissions") }
                    },
                    dismissButton = {
                        Button(onClick = { showRationaleDialog = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

// TODO: Implement ComposeFileProvider class (likely in core module)
// This is a placeholder structure
object ComposeFileProvider {
    // Function to create a temporary image file and return its content URI
    fun getImageUri(context: Context): Uri? {
        try {
            // Create an image file name with a timestamp
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"

            // Get the cache directory using the path defined in file_paths.xml
            // Ensure the subdirectory matches the 'path' attribute in file_paths.xml (<cache-path name="captured_images" path="images/"/>)
            val storageDir = File(context.cacheDir, "images")
            storageDir.mkdirs() // Ensure the directory exists

            val imageFile = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg",        /* suffix */
                storageDir     /* directory */
            )

            // Get the content URI using the FileProvider
            // The authority must match the one declared in AndroidManifest.xml
            val authority = "${context.packageName}.provider"
            return FileProvider.getUriForFile(
                Objects.requireNonNull(context),
                authority,
                imageFile
            )
        } catch (e: Exception) {
            // Log the error or handle it appropriately
            println("Error creating image URI: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
}


// Basic Preview (Unchanged)
@Preview(showBackground = true)
@Composable
fun ReactantScreenPreview() {
    // val previewState = ReactantScreenState() // Removed unused variable
    MaterialTheme {
         ReactantScreen(
             viewModel = ReactantViewModel(),
             // Removed unused navigation callbacks from preview
             onNavigateToArProduct = {}
         )
    }
}