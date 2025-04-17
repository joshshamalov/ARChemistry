package com.joshs.archemistry.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import com.joshs.archemistry.ui.theme.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joshs.archemistry.graph.Atom
import com.joshs.archemistry.graph.MolecularGraph
import com.joshs.archemistry.reaction.Reagent
import com.joshs.archemistry.utils.Logger
import com.joshs.archemistry.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToAR: (String, String, String) -> Unit,
    onNavigateToBackend: (String) -> Unit,
    onNavigateToDebugLogs: () -> Unit,
    onNavigateToCrop: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Get state from ViewModel
    val selectedImageUri = viewModel.selectedImageUri
    val selectedImageBitmap = viewModel.selectedImageBitmap
    val selectedReagent = viewModel.selectedReagent

    // Local UI state
    var expanded by remember { mutableStateOf(false) }

    // State for processing
    var isProcessing by remember { mutableStateOf(false) }

    // Available reagents
    val reagents = listOf(
        Reagent("H₂ (Hydrogen)", "Hydrogenation"),
        Reagent("Br₂ (Bromine)", "Bromination"),
        Reagent("KMnO₄ (Potassium permanganate)", "Dihydroxylation")
    )

    // Image selection launcher for gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            // Clear all image data including original
            viewModel.clearAllImageData()
            // Set this as the original image immediately
            viewModel.setOriginalImage(uri, null)
            // Navigate to the crop screen with the selected image URI
            onNavigateToCrop(uri.toString())
        }
    }

    // Create a temporary file for the camera image
    val tempImageFile = remember {
        File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            context.cacheDir
        ).apply {
            deleteOnExit()
        }
    }

    // Remember the URI for the temporary file
    val tempImageUri = remember {
        androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempImageFile
        )
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Clear all image data including original
            viewModel.clearAllImageData()
            // Set this as the original image immediately
            viewModel.setOriginalImage(tempImageUri, null)
            // Navigate to the crop screen with the captured image URI
            onNavigateToCrop(tempImageUri.toString())
            Logger.log("Camera image captured successfully")
        } else {
            Logger.log("Camera capture failed or was cancelled")
        }
    }

    // Permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, launch camera
            cameraLauncher.launch(tempImageUri)
            Logger.log("Camera permission granted, launching camera")
        } else {
            // Permission denied
            Logger.log("Camera permission denied")
            // Show a message to the user
            Toast.makeText(context, "Camera permission is required to capture images", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ARChemistry",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = ARPrimaryBlue
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image selection area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ARCardBackground)
                    .border(1.dp, ARButtonGray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageBitmap != null) {
                    Image(
                        bitmap = selectedImageBitmap!!.asImageBitmap(),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // Edit button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ARButtonGray)
                            .clickable {
                                // Navigate to crop screen with current image
                                if (viewModel.selectedImageUri != null) {
                                    // Make sure we have the original image stored
                                    if (viewModel.originalImageUri == null) {
                                        // If no original image is stored, use the current image as original
                                        viewModel.setOriginalImage(viewModel.selectedImageUri, viewModel.selectedImageBitmap)
                                    }
                                    // Always navigate with the current image
                                    onNavigateToCrop(viewModel.selectedImageUri.toString())
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Image",
                            tint = ARTextPrimary
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No image selected",
                            fontSize = 20.sp,
                            color = ARTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Capture or select an image of a chemical reaction",
                            fontSize = 14.sp,
                            color = ARTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }

            // Image selection buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        // Request camera permission and launch camera
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        Logger.log("Camera capture requested")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ARButtonGray,
                        contentColor = ARTextPrimary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Capture"
                        )
                        Text(
                            text = "Capture Image",
                            fontSize = 14.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(start = 8.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ARButtonGray,
                        contentColor = ARTextPrimary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Gallery"
                        )
                        Text(
                            text = "Select from Gallery",
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Reagent selection
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Select Reagent:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ARTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedReagent?.name ?: "Select a reagent",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = ARButtonGray,
                            focusedBorderColor = ARTextPrimary
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        reagents.forEach { reagent ->
                            DropdownMenuItem(
                                text = { Text(reagent.name) },
                                onClick = {
                                    viewModel.updateSelectedReagent(reagent)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Process Reaction button
            Button(
                onClick = {
                    if (selectedImageBitmap != null && selectedReagent != null) {
                        isProcessing = true

                        coroutineScope.launch {
                            try {
                                // Process the image and execute the reaction
                                Logger.log("Starting reaction processing")

                                // For the MVP, we'll create a simple molecular graph
                                // In a real implementation, we would use the StructureRecognizer
                                Logger.log("Creating a simple ethene graph for demo purposes")
                                val graph = createSimpleEtheneGraph()

                                // Execute the reaction
                                val reactionEngine = ReactionEngine()
                                val product = reactionEngine.executeReaction(
                                    graph,
                                    selectedReagent!!
                                )

                                // Save the reactant and product graphs
                                val reactantPath = saveGraphToFile(context, graph, "reactant")
                                val productPath = saveGraphToFile(context, product, "product")

                                Logger.log("Saved reactant to $reactantPath")
                                Logger.log("Saved product to $productPath")

                                // Navigate to AR screen
                                onNavigateToAR(
                                    reactantPath,
                                    selectedReagent!!.name,
                                    productPath
                                )

                            } catch (e: Exception) {
                                Logger.error("Error processing reaction: ${e.message}", e)
                            } finally {
                                isProcessing = false
                            }
                        }
                    }
                },
                enabled = selectedImageBitmap != null && selectedReagent != null && !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ARButtonGray,
                    contentColor = ARTextPrimary,
                    disabledContainerColor = ARDisabledGray,
                    disabledContentColor = ARTextSecondary
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = ARTextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Process Reaction",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Backend button
            Button(
                onClick = {
                    onNavigateToBackend("default")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ARPrimaryBlue,
                    contentColor = ARTextPrimary
                )
            ) {
                Text(
                    text = "Backend",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Debug Logs button
            Button(
                onClick = {
                    onNavigateToDebugLogs()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ARSecondaryPurple,
                    contentColor = ARTextPrimary
                )
            ) {
                Text(
                    text = "View Debug Logs",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Temporary function to create a file URI for testing.
 */
private fun createTempImageFile(context: Context): Uri {
    val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
        // Create a simple 100x100 bitmap
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return Uri.fromFile(file)
}

/**
 * Saves a molecular graph to a file and returns the file path.
 */
private fun saveGraphToFile(context: Context, graph: MolecularGraph, prefix: String): String {
    val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.ser")
    file.outputStream().use { out ->
        java.io.ObjectOutputStream(out).use { oos ->
            oos.writeObject(graph)
        }
    }
    return file.absolutePath
}

/**
 * Creates a simple ethene (C=C) graph for testing.
 */
private fun createSimpleEtheneGraph(): MolecularGraph {
    val graph = MolecularGraph()

    // Create two carbon atoms
    val carbon1 = Atom("C", x = 0f, y = 0f, x3d = -0.5f, y3d = 0f, z3d = 0f)
    val carbon2 = Atom("C", x = 50f, y = 0f, x3d = 0.5f, y3d = 0f, z3d = 0f)

    // Add atoms to the graph
    graph.addVertex(carbon1)
    graph.addVertex(carbon2)

    // Add a double bond between them
    graph.addBond(carbon1, carbon2, 2)

    // Add implicit hydrogens
    graph.addImplicitHydrogens()

    return graph
}

/**
 * Simple reaction engine for the MVP.
 */
class ReactionEngine {
    fun executeReaction(reactant: MolecularGraph, reagent: Reagent): MolecularGraph {
        // Clone the reactant graph
        val product = reactant.clone()

        // Apply reaction based on reagent
        when (reagent.name) {
            "H₂ (Hydrogen)" -> applyHydrogenation(product)
            "Br₂ (Bromine)" -> applyBromination(product)
            "KMnO₄ (Potassium permanganate)" -> applyDihydroxylation(product)
        }

        return product
    }

    private fun applyHydrogenation(graph: MolecularGraph) {
        // Find all double bonds and convert them to single bonds
        val doubleBonds = graph.edgeSet().filter { graph.getEdgeWeight(it) == 2.0 }.toList()

        for (bond in doubleBonds) {
            // Convert double bond to single bond
            graph.setEdgeWeight(bond, 1.0)

            // Add hydrogens to the atoms
            val source = graph.getEdgeSource(bond)
            val target = graph.getEdgeTarget(bond)

            source.implicitHydrogens += 1
            target.implicitHydrogens += 1
        }
    }

    private fun applyBromination(graph: MolecularGraph) {
        // Find all double bonds and convert them to single bonds, adding Br to each carbon
        val doubleBonds = graph.edgeSet().filter { graph.getEdgeWeight(it) == 2.0 }.toList()

        for (bond in doubleBonds) {
            // Convert double bond to single bond
            graph.setEdgeWeight(bond, 1.0)

            // Get the carbon atoms
            val carbon1 = graph.getEdgeSource(bond)
            val carbon2 = graph.getEdgeTarget(bond)

            // Create bromine atoms
            val bromine1 = Atom("Br",
                x = carbon1.x + 20f,
                y = carbon1.y + 20f,
                x3d = carbon1.x3d + 0.5f,
                y3d = carbon1.y3d + 0.5f,
                z3d = carbon1.z3d
            )

            val bromine2 = Atom("Br",
                x = carbon2.x + 20f,
                y = carbon2.y + 20f,
                x3d = carbon2.x3d + 0.5f,
                y3d = carbon2.y3d + 0.5f,
                z3d = carbon2.z3d
            )

            // Add bromine atoms to the graph
            graph.addVertex(bromine1)
            graph.addVertex(bromine2)

            // Add bonds between carbon and bromine
            graph.addEdge(carbon1, bromine1)
            graph.addEdge(carbon2, bromine2)
        }
    }

    private fun applyDihydroxylation(graph: MolecularGraph) {
        // Find all double bonds and convert them to single bonds, adding OH to each carbon
        val doubleBonds = graph.edgeSet().filter { graph.getEdgeWeight(it) == 2.0 }.toList()

        for (bond in doubleBonds) {
            // Convert double bond to single bond
            graph.setEdgeWeight(bond, 1.0)

            // Get the carbon atoms
            val carbon1 = graph.getEdgeSource(bond)
            val carbon2 = graph.getEdgeTarget(bond)

            // Create oxygen atoms
            val oxygen1 = Atom("O",
                x = carbon1.x + 20f,
                y = carbon1.y + 20f,
                x3d = carbon1.x3d + 0.5f,
                y3d = carbon1.y3d + 0.5f,
                z3d = carbon1.z3d
            )

            val oxygen2 = Atom("O",
                x = carbon2.x + 20f,
                y = carbon2.y + 20f,
                x3d = carbon2.x3d + 0.5f,
                y3d = carbon2.y3d + 0.5f,
                z3d = carbon2.z3d
            )

            // Add oxygen atoms to the graph
            graph.addVertex(oxygen1)
            graph.addVertex(oxygen2)

            // Add bonds between carbon and oxygen
            graph.addEdge(carbon1, oxygen1)
            graph.addEdge(carbon2, oxygen2)

            // Add implicit hydrogens to oxygen (OH groups)
            oxygen1.implicitHydrogens = 1
            oxygen2.implicitHydrogens = 1
        }
    }
}
