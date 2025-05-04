package com.joshs.archemistry.feature_ar_product

import android.app.Activity
import android.util.Log
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner // Updated import
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.ar.core.ArCoreApk
import com.google.ar.core.ArCoreApk.Availability
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dev.romainguy.kotlin.math.*
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.gesture.*
import io.github.sceneview.math.Position
import io.github.sceneview.node.CylinderNode
import io.github.sceneview.node.Node
import io.github.sceneview.node.SphereNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberMaterialLoader
import kotlinx.coroutines.launch

// Data class for individual molecule structure (reactant or product)
// Make fields nullable to match ApiService.kt definition
data class MoleculeData(
    @SerializedName("atoms") val atoms: List<String>? = null,
    @SerializedName("coords") val coords: List<List<Double>>? = null,
    @SerializedName("bonds") val bonds: List<List<Int>>? = null,
    @SerializedName("error") val error: String? = null
)

// Data class for the nested response from the server
data class ReactionResponse(
    @SerializedName("reactant") val reactant: MoleculeData? = null,
    @SerializedName("product") val product: MoleculeData? = null,
    @SerializedName("error") val error: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArProductScreen(modelDataJson: String?) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var arCoreAvailability by remember { mutableStateOf<Availability?>(null) }
    var isArCoreInstallRequested by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    // Function to check ARCore availability
    fun checkArCoreAvailability() {
        coroutineScope.launch {
            arCoreAvailability = ArCoreApk.getInstance().checkAvailability(context)
            // If an install was requested previously, reset the flag after checking again
            if (isArCoreInstallRequested) {
                isArCoreInstallRequested = false
            }
        }
    }

    // Function to request ARCore installation
    fun requestArCoreInstallation() {
        if (context is Activity) {
            try {
                // Request installation - this requires user interaction
                val installStatus = ArCoreApk.getInstance().requestInstall(context, !isArCoreInstallRequested)
                if (installStatus == ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
                    isArCoreInstallRequested = true // Set flag to avoid multiple requests until check runs again
                }
                // Note: The result of the installation will be reflected in the next availability check (e.g., onResume)
            } catch (e: UnavailableUserDeclinedInstallationException) {
                errorMessage = "ARCore installation declined by user."
            } catch (e: Exception) {
                errorMessage = "Failed to request ARCore installation: ${e.message}"
            }
        } else {
            errorMessage = "Cannot request ARCore installation: Context is not an Activity."
        }
    }

    // Check ARCore availability on launch and resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkArCoreAvailability()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Initial check if availability is still null
    LaunchedEffect(Unit) {
        if (arCoreAvailability == null) {
            checkArCoreAvailability()
        }
    }


    // Handle initial data error separately
    if (modelDataJson == null && errorMessage == null && arCoreAvailability != null && arCoreAvailability!!.isSupported) {
        LaunchedEffect(Unit) { // Use LaunchedEffect to set state safely
            errorMessage = "No model data received."
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("AR Product Viewer") }) }) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (arCoreAvailability) {
                null -> {
                    // Still checking availability
                    CircularProgressIndicator()
                    Text("Checking AR compatibility...")
                }
                Availability.SUPPORTED_INSTALLED -> {
                    // ARCore is ready, check for other errors or show AR view
                    if (errorMessage != null) {
                        Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                    } else if (modelDataJson != null) {
                        BasicARView(modelDataJson)
                    } else {
                        // Should be caught by the earlier check, but as a fallback
                        Text("Error: Model data is missing.", color = MaterialTheme.colorScheme.error)
                    }
                }
                Availability.SUPPORTED_APK_TOO_OLD, Availability.SUPPORTED_NOT_INSTALLED -> {
                    // Needs install or update
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (arCoreAvailability == Availability.SUPPORTED_NOT_INSTALLED) "ARCore installation required." else "ARCore update required.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = ::requestArCoreInstallation, enabled = !isArCoreInstallRequested) {
                            Text(if (arCoreAvailability == Availability.SUPPORTED_NOT_INSTALLED) "Install ARCore" else "Update ARCore")
                        }
                        if (isArCoreInstallRequested) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Installation requested. Please follow system prompts.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> {
                    Text("Error: This device does not support AR.", color = MaterialTheme.colorScheme.error)
                }
                Availability.UNKNOWN_CHECKING, Availability.UNKNOWN_ERROR, Availability.UNKNOWN_TIMED_OUT -> {
                    Text("Error: Could not check AR compatibility.", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

fun getAtomProperties(symbol: String): Pair<Color, Float> = when (symbol.uppercase()) {
    "H" -> Color.White to 0.25f
    "C" -> Color.DarkGray to 0.4f
    "N" -> Color.Blue to 0.35f
    "O" -> Color.Red to 0.35f
    "F", "CL" -> Color.Green to 0.35f // Uppercase CL
    "BR" -> Color(0.6f, 0.1f, 0.1f) to 0.4f // Uppercase BR
    "I" -> Color(0.4f, 0.0f, 0.6f) to 0.45f
    "P" -> Color(1.0f, 0.5f, 0.0f) to 0.45f
    "S" -> Color.Yellow to 0.45f
    else -> Color(0.8f, 0.5f, 0.8f) to 0.4f
}

@Composable
fun BasicARView(modelDataJson: String?) {
    val context = LocalContext.current
    val engine = rememberEngine()
    val materialLoader = rememberMaterialLoader(engine)
    val mainLightNode = rememberMainLightNode(engine).apply {
        intensity = 100_000f // Keep directional light, IBL provides ambient
        // Optionally adjust direction if needed: direction = Float3(0.0f, -1.0f, -0.5f)
    }
    val cameraNode = rememberARCameraNode(engine)
    val moleculeNode = remember { Node(engine) }
    val childNodes = remember { mutableStateListOf<Node>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // State for the button text toggle
    var showProductText by remember { mutableStateOf(true) }

    // Make LaunchedEffect depend on showProductText to rebuild when button is clicked
    LaunchedEffect(modelDataJson, showProductText) {
        if (modelDataJson == null) {
            errorMessage = "Model data is null."
            return@LaunchedEffect
        }

        // Parse the JSON as a ReactionResponse
        val reactionResponse = try {
            Gson().fromJson(modelDataJson, ReactionResponse::class.java)
        } catch (e: Exception) {
            errorMessage = "Failed to parse model data: ${e.message}"
            return@LaunchedEffect
        }

        // Check for overall error
        if (reactionResponse?.error != null) {
            errorMessage = "Server error: ${reactionResponse.error}"
            return@LaunchedEffect
        }

        // Select the appropriate molecule data based on showProductText
        val moleculeData = if (showProductText) {
            reactionResponse.product
        } else {
            reactionResponse.reactant
        }

        // Check if the selected molecule data is valid
        if (moleculeData == null) {
            errorMessage = if (showProductText) "Product data is missing." else "Reactant data is missing."
            return@LaunchedEffect
        }

        // Use safe calls to avoid smart casting issues
        if (moleculeData?.error != null) {
            errorMessage = if (showProductText) "Product error: ${moleculeData.error}" else "Reactant error: ${moleculeData.error}"
            return@LaunchedEffect
        }

        // Use safe calls with elvis operator to handle potential nulls
        val atoms = moleculeData?.atoms ?: emptyList()
        if (atoms.isEmpty()) {
            errorMessage = if (showProductText) "Product data is empty." else "Reactant data is empty."
            return@LaunchedEffect
        }

        moleculeNode.childNodes.toList().forEach {
            moleculeNode.removeChildNode(it)
            it.destroy()
        }
        childNodes.clear()

        // Use safe calls with elvis operator for all properties
        val coords = moleculeData?.coords ?: emptyList()
        val bonds = moleculeData?.bonds ?: emptyList()
        Log.d("ARChemDebug", "Received ${atoms.size} atoms and ${bonds.size} bonds.") // Log bond count
        val atomNodes = mutableMapOf<Int, SphereNode>()
        val bondRadius = 0.08f
        val scaleFactor = 0.4f

        atoms.forEachIndexed { index, symbol ->
            val coord = coords.getOrNull(index) ?: return@forEachIndexed
            val (color, radius) = getAtomProperties(symbol)
            val material = materialLoader.createColorInstance(color)
            val position = Position(
                coord[0].toFloat() * scaleFactor,
                coord[1].toFloat() * scaleFactor,
                coord[2].toFloat() * scaleFactor
            )
            val node = SphereNode(engine, radius * scaleFactor, position).apply {
                materialInstance = material
            }
            atomNodes[index] = node
            Log.d("ARChemDebug", "Atom $index ($symbol) intended position: $position") // Log intended position
            moleculeNode.addChildNode(node)
        }

        // Log the parent node's position before processing bonds
        Log.d("ARChemDebug", "MoleculeNode worldPosition before bond loop: ${moleculeNode.worldPosition}")

        bonds.forEachIndexed { index, bond -> // Use forEachIndexed for logging
            Log.d("ARChemDebug", "Processing bond $index: ${bond.joinToString()}")
            if (bond.size < 2) {
                Log.w("ARChemDebug", "Skipping bond $index: Invalid size ${bond.size}")
                return@forEachIndexed
            }
            val startNodeIndex = bond[0]
            val endNodeIndex = bond[1]
            val start = atomNodes[startNodeIndex]
            val end = atomNodes[endNodeIndex]

            if (start == null) {
                Log.w("ARChemDebug", "Skipping bond $index: Start node not found for index $startNodeIndex")
                return@forEachIndexed
            }
            if (end == null) {
                Log.w("ARChemDebug", "Skipping bond $index: End node not found for index $endNodeIndex")
                return@forEachIndexed
            }
            Log.d("ARChemDebug", "Found start and end nodes for bond $index.")

            // Get original coordinates from the server data
            val coord1Raw = coords.getOrNull(startNodeIndex)
            val coord2Raw = coords.getOrNull(endNodeIndex)

            if (coord1Raw == null || coord2Raw == null || coord1Raw.size < 3 || coord2Raw.size < 3) {
                Log.w("ARChemDebug", "Skipping bond $index: Invalid coordinate data.")
                return@forEachIndexed
            }

            // Create Float3 vectors from raw coords and apply scaling
            val pos1Scaled = Float3(coord1Raw[0].toFloat() * scaleFactor, coord1Raw[1].toFloat() * scaleFactor, coord1Raw[2].toFloat() * scaleFactor)
            val pos2Scaled = Float3(coord2Raw[0].toFloat() * scaleFactor, coord2Raw[1].toFloat() * scaleFactor, coord2Raw[2].toFloat() * scaleFactor)
            Log.d("ARChemDebug", "ScaledPos1: $pos1Scaled, ScaledPos2: $pos2Scaled for bond $index") // Log scaled positions

            // Calculate midpoint, difference, length, and direction from scaled positions
            val midScaled = (pos1Scaled + pos2Scaled) / 2f
            val diffScaled = pos2Scaled - pos1Scaled
            val lenScaled = length(diffScaled)
            val dirScaled = normalize(diffScaled)

            // --- Original Cylinder code restored ---
            // Add check for non-zero length before creating cylinder
            if (lenScaled > 1e-4f) { // Use a small tolerance
                val material = materialLoader.createColorInstance(Color.Gray)
                // Create cylinder centered at the origin
                // Create cylinder centered at the origin
                val cylinder = CylinderNode(
                    engine = engine,
                    radius = bondRadius * scaleFactor,
                    height = lenScaled,
                    center = Position(x = 0f, y = 0f, z = 0f) // Origin
                ).apply {
                    materialInstance = material
                    // Calculate rotation to align default Y with bond direction
                    val defaultDir = Float3(0f, 1f, 0f) // Cylinder's default orientation axis (Assuming Y-up)
                    val rotationAxis = normalize(cross(defaultDir, dirScaled))
                    // Clamp dot product to avoid NaN from acos due to floating point errors
                    val dotProd = clamp(dot(defaultDir, dirScaled), -1.0f, 1.0f)
                    val angleRad = kotlin.math.acos(dotProd) // Angle in radians

                    // Apply rotation if axis is valid (vectors are not collinear)
                    // Check if vectors are nearly collinear (dot product close to 1 or -1)
                    if (kotlin.math.abs(dotProd) < 0.9999f) {
                        // Axis is valid, calculate quaternion
                        quaternion = Quaternion.fromAxisAngle(rotationAxis, degrees(angleRad))
                    } else if (dotProd < -0.999f) {
                        // Vectors are opposite (180 degrees)
                        // Rotate 180 degrees around any perpendicular axis (e.g., X-axis)
                        quaternion = Quaternion.fromAxisAngle(Float3(1f, 0f, 0f), 180f)
                    }
                    // If vectors are identical (dot product is ~1), no rotation is needed (quaternion remains identity).

                    // Set position to the midpoint *after* rotation
                    position = midScaled
                }
                Log.d("ARChemDebug", "Adding cylinder for bond $index to moleculeNode.")
                moleculeNode.addChildNode(cylinder)
                Log.d("ARChemDebug", "Cylinder for bond $index added.")
            } else {
                Log.w("ARChemDebug", "Skipping cylinder for bond $index: Zero or near-zero length ($lenScaled)")
            }
        }

        moleculeNode.position = Position(z = -1.5f)
        childNodes.add(moleculeNode)
        errorMessage = null
    }

    // Wrap everything in a Box to allow for proper UI layering
    Box(modifier = Modifier.fillMaxSize()) {
        // AR Scene takes the full size of the Box
        ARScene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            cameraNode = cameraNode,
            childNodes = childNodes + listOfNotNull(mainLightNode),
            planeRenderer = false,
            activity = context as? ComponentActivity,
            onGestureListener = object : GestureDetector.OnGestureListener {
                override fun onSingleTapConfirmed(e: MotionEvent, node: Node?) {}
                override fun onSingleTapUp(e: MotionEvent, node: Node?) {}
                override fun onDown(e: MotionEvent, node: Node?) {}
                override fun onShowPress(e: MotionEvent, node: Node?) {}
                override fun onContextClick(e: MotionEvent, node: Node?) {}
                override fun onDoubleTap(e: MotionEvent, node: Node?) {}
                override fun onDoubleTapEvent(e: MotionEvent, node: Node?) {}
                override fun onLongPress(e: MotionEvent, node: Node?) {}
                override fun onMove(detector: MoveGestureDetector, e: MotionEvent, node: Node?) {}
                override fun onMoveBegin(detector: MoveGestureDetector, e: MotionEvent, node: Node?) {}
                override fun onMoveEnd(detector: MoveGestureDetector, e: MotionEvent, node: Node?) {}
                override fun onRotateBegin(detector: RotateGestureDetector, e: MotionEvent, node: Node?) {}
                override fun onRotate(detector: RotateGestureDetector, e: MotionEvent, node: Node?) {}
                override fun onRotateEnd(detector: RotateGestureDetector, e: MotionEvent, node: Node?) {}
                override fun onScaleBegin(detector: ScaleGestureDetector, e: MotionEvent, node: Node?) {}
                override fun onScale(detector: ScaleGestureDetector, e: MotionEvent, node: Node?) {
                    if (node == null || node == moleculeNode || node.parent == moleculeNode) {
                        moleculeNode.scale *= detector.scaleFactor
                    }
                }
                override fun onScaleEnd(detector: ScaleGestureDetector, e: MotionEvent, node: Node?) {}
                override fun onScroll(e1: MotionEvent?, e2: MotionEvent, node: Node?, distance: Float2) {}
                override fun onFling(e1: MotionEvent?, e2: MotionEvent, node: Node?, velocity: Float2) {}
            }
        )
        
        // Add the toggle button UI inside the Box, on top of the ARScene
        Button(
            onClick = { showProductText = !showProductText }, // Toggle the text state
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            // Enable only if both reactant and product data are available
            enabled = true // We'll keep it always enabled for simplicity
        ) {
            // Button text shows what will be displayed next (not what's currently shown)
            Text(if (showProductText) "Show Reactant" else "Show Product")
        }
        
        // Display Error Message if any (related to AR view loading)
        if (errorMessage != null) {
            Text(
                text = "Error: $errorMessage",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }
    }
}
