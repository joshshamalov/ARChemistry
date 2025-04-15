package com.joshs.archemistry.ui

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.joshs.archemistry.graph.MolecularGraph
import com.joshs.archemistry.rendering.MoleculeRenderer
import com.joshs.archemistry.utils.Logger
import android.view.View
import java.io.File
import java.io.ObjectInputStream

/**
 * Screen that displays the product molecule in AR.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARScreen(
    @Suppress("UNUSED_PARAMETER") reactantImage: String?,
    @Suppress("UNUSED_PARAMETER") reagent: String?,
    product: String?,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Load the product graph
    val molecularGraph = remember {
        if (product != null) {
            try {
                loadGraphFromFile(context, product)
            } catch (e: Exception) {
                Logger.error("Error loading product graph: ${e.message}", e)
                null
            }
        } else {
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AR View") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // State for the AR scene
            var sceneView by remember { mutableStateOf<View?>(null) }
            var modelNode by remember { mutableStateOf<Any?>(null) }

            // Create the AR scene
            AndroidView(
                factory = { ctx ->
                    // Create a simple view for the MVP
                    android.view.View(ctx).apply {
                        // Set background color
                        setBackgroundColor(android.graphics.Color.BLACK)

                        try {
                            // If we have a molecular graph, render it
                            if (molecularGraph != null) {
                                // Create the molecule renderer
                                val renderer = MoleculeRenderer(context)

                                // Render the molecule
                                modelNode = renderer.renderMoleculeInAR(this, molecularGraph!!)
                                Logger.log("Placed molecule in scene")
                            } else {
                                Logger.log("No molecular graph available to render")
                            }
                        } catch (e: Exception) {
                            Logger.error("Error rendering molecule: ${e.message}", e)
                        }

                        sceneView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Clean up resources when the composable is disposed
            DisposableEffect(Unit) {
                onDispose {
                    // No cleanup needed for the simplified implementation
                    Logger.log("ARView disposed")
                }
            }
        }
    }
}

/**
 * Loads a molecular graph from a file.
 *
 * @throws Exception if the file cannot be read or deserialized
 */
private fun loadGraphFromFile(@Suppress("UNUSED_PARAMETER") context: Context, filePath: String): MolecularGraph {
    val file = File(filePath)
    if (!file.exists()) {
        throw IllegalArgumentException("File does not exist: $filePath")
    }

    try {
        return file.inputStream().use { input ->
            ObjectInputStream(input).use { ois ->
                ois.readObject() as MolecularGraph
            }
        }
    } catch (e: Exception) {
        Logger.error("Failed to load graph from file: ${e.message}")
        throw e
    }
}
