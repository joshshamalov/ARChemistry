package com.joshs.archemistry.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joshs.archemistry.utils.Logger
import com.joshs.archemistry.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * A screen for cropping and rotating images.
 * This implementation uses a simplified approach with preset crop ratios.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropScreen(
    viewModel: MainViewModel,
    originalImageUri: Uri,
    onCropComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    
    // State for the original bitmap
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    // State for the current bitmap (after rotation)
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    // State for loading indicator
    var isLoading by remember { mutableStateOf(true) }
    
    // State for the rotation angle
    var rotationAngle by remember { mutableStateOf(0f) }
    
    // State for image container dimensions
    var imageContainerWidth by remember { mutableStateOf(0f) }
    var imageContainerHeight by remember { mutableStateOf(0f) }
    
    // State for image dimensions and position within container
    var imageWidth by remember { mutableStateOf(0f) }
    var imageHeight by remember { mutableStateOf(0f) }
    var imageX by remember { mutableStateOf(0f) }
    var imageY by remember { mutableStateOf(0f) }
    
    // State for crop ratio selection
    var selectedRatio by remember { mutableStateOf("FREE") } // FREE, SQUARE, 4:3, 16:9
    
    // Load the bitmap when the screen is first composed
    LaunchedEffect(originalImageUri) {
        try {
            val bitmap = loadBitmapFromUri(context, originalImageUri)
            if (bitmap != null) {
                originalBitmap = bitmap
                currentBitmap = bitmap
                viewModel.setOriginalImage(originalImageUri, bitmap)
                isLoading = false
            } else {
                Logger.error("Failed to load bitmap from URI: $originalImageUri")
                onCancel()
            }
        } catch (e: Exception) {
            Logger.error("Error loading bitmap: ${e.message}", e)
            onCancel()
        }
    }
    
    // Update the current bitmap when rotation changes
    LaunchedEffect(rotationAngle, originalBitmap) {
        originalBitmap?.let { bitmap ->
            currentBitmap = rotateBitmap(bitmap, rotationAngle)
        }
    }
    
    // Calculate the crop rectangle based on image dimensions and selected ratio
    val cropRect = remember(imageWidth, imageHeight, imageX, imageY, selectedRatio) {
        if (imageWidth > 0 && imageHeight > 0) {
            // Calculate crop dimensions based on selected ratio
            val (cropWidth, cropHeight) = when (selectedRatio) {
                "SQUARE" -> {
                    val size = min(imageWidth, imageHeight) * 0.9f
                    Pair(size, size)
                }
                "4:3" -> {
                    if (imageWidth > imageHeight) {
                        val height = imageHeight * 0.9f
                        val width = height * 4f / 3f
                        if (width > imageWidth) {
                            val adjustedWidth = imageWidth * 0.9f
                            val adjustedHeight = adjustedWidth * 3f / 4f
                            Pair(adjustedWidth, adjustedHeight)
                        } else {
                            Pair(width, height)
                        }
                    } else {
                        val width = imageWidth * 0.9f
                        val height = width * 3f / 4f
                        if (height > imageHeight) {
                            val adjustedHeight = imageHeight * 0.9f
                            val adjustedWidth = adjustedHeight * 4f / 3f
                            Pair(adjustedWidth, adjustedHeight)
                        } else {
                            Pair(width, height)
                        }
                    }
                }
                "16:9" -> {
                    if (imageWidth > imageHeight) {
                        val height = imageHeight * 0.9f
                        val width = height * 16f / 9f
                        if (width > imageWidth) {
                            val adjustedWidth = imageWidth * 0.9f
                            val adjustedHeight = adjustedWidth * 9f / 16f
                            Pair(adjustedWidth, adjustedHeight)
                        } else {
                            Pair(width, height)
                        }
                    } else {
                        val width = imageWidth * 0.9f
                        val height = width * 9f / 16f
                        if (height > imageHeight) {
                            val adjustedHeight = imageHeight * 0.9f
                            val adjustedWidth = adjustedHeight * 16f / 9f
                            Pair(adjustedWidth, adjustedHeight)
                        } else {
                            Pair(width, height)
                        }
                    }
                }
                else -> { // FREE
                    val width = imageWidth * 0.8f
                    val height = imageHeight * 0.8f
                    Pair(width, height)
                }
            }
            
            // Center the crop rectangle
            val cropX = imageX + (imageWidth - cropWidth) / 2
            val cropY = imageY + (imageHeight - cropHeight) / 2
            
            Rect(cropX, cropY, cropX + cropWidth, cropY + cropHeight)
        } else {
            null
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Crop Image",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3) // Material Blue
                ),
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            currentBitmap?.let { bitmap ->
                                cropRect?.let { rect ->
                                    isLoading = true
                                    coroutineScope.launch {
                                        try {
                                            // Convert screen coordinates to bitmap coordinates
                                            val bitmapRect = convertScreenRectToBitmapRect(
                                                rect,
                                                bitmap.width,
                                                bitmap.height,
                                                imageX,
                                                imageY,
                                                imageWidth,
                                                imageHeight
                                            )
                                            
                                            // Crop the bitmap
                                            val croppedBitmap = cropBitmap(bitmap, bitmapRect)
                                            
                                            // Save the cropped bitmap to a file
                                            val processedUri = saveBitmapToFile(context, croppedBitmap)
                                            
                                            // Update the ViewModel with the processed image
                                            viewModel.updateSelectedImage(processedUri, croppedBitmap)
                                            
                                            // Navigate back
                                            onCropComplete()
                                        } catch (e: Exception) {
                                            Logger.error("Error processing image: ${e.message}", e)
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done"
                            )
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            currentBitmap?.let { bitmap ->
                // Image container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(
                            bitmap.width.toFloat() / bitmap.height.toFloat(),
                            matchHeightConstraintsFirst = bitmap.width < bitmap.height
                        )
                        .onGloballyPositioned { coordinates ->
                            imageContainerWidth = coordinates.size.width.toFloat()
                            imageContainerHeight = coordinates.size.height.toFloat()
                        }
                ) {
                    // Display the image
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Image to crop",
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { coordinates ->
                                // Calculate image dimensions and position within container
                                val containerAspectRatio = imageContainerWidth / imageContainerHeight
                                val imageAspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                                
                                if (imageAspectRatio > containerAspectRatio) {
                                    // Image is wider than container
                                    imageWidth = imageContainerWidth
                                    imageHeight = imageWidth / imageAspectRatio
                                    imageX = 0f
                                    imageY = (imageContainerHeight - imageHeight) / 2
                                } else {
                                    // Image is taller than container
                                    imageHeight = imageContainerHeight
                                    imageWidth = imageHeight * imageAspectRatio
                                    imageY = 0f
                                    imageX = (imageContainerWidth - imageWidth) / 2
                                }
                            },
                        contentScale = ContentScale.Fit
                    )
                    
                    // Crop overlay
                    cropRect?.let { rect ->
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw semi-transparent overlay outside crop area
                            drawRect(
                                color = Color.Black.copy(alpha = 0.5f),
                                topLeft = Offset(0f, 0f),
                                size = Size(size.width, rect.top)
                            )
                            drawRect(
                                color = Color.Black.copy(alpha = 0.5f),
                                topLeft = Offset(0f, rect.bottom),
                                size = Size(size.width, size.height - rect.bottom)
                            )
                            drawRect(
                                color = Color.Black.copy(alpha = 0.5f),
                                topLeft = Offset(0f, rect.top),
                                size = Size(rect.left, rect.height)
                            )
                            drawRect(
                                color = Color.Black.copy(alpha = 0.5f),
                                topLeft = Offset(rect.right, rect.top),
                                size = Size(size.width - rect.right, rect.height)
                            )
                            
                            // Draw crop rectangle border
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(rect.left, rect.top),
                                size = Size(rect.width, rect.height),
                                style = Stroke(width = 2.dp.toPx())
                            )
                            
                            // Draw grid lines
                            val thirdWidth = rect.width / 3f
                            val thirdHeight = rect.height / 3f
                            
                            // Vertical grid lines
                            drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = Offset(rect.left + thirdWidth, rect.top),
                                end = Offset(rect.left + thirdWidth, rect.bottom),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = Offset(rect.left + 2 * thirdWidth, rect.top),
                                end = Offset(rect.left + 2 * thirdWidth, rect.bottom),
                                strokeWidth = 1.dp.toPx()
                            )
                            
                            // Horizontal grid lines
                            drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = Offset(rect.left, rect.top + thirdHeight),
                                end = Offset(rect.right, rect.top + thirdHeight),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = Offset(rect.left, rect.top + 2 * thirdHeight),
                                end = Offset(rect.right, rect.top + 2 * thirdHeight),
                                strokeWidth = 1.dp.toPx()
                            )
                            
                            // Draw corner handles
                            val handleRadius = 12.dp.toPx()
                            
                            // Draw handles with a white fill and blue border for better visibility
                            val handleColor = Color.White
                            val handleBorderColor = Color(0xFF2196F3) // Material Blue
                            val handleBorderWidth = 2.dp.toPx()
                            
                            // Top-left handle
                            drawCircle(
                                color = handleColor,
                                radius = handleRadius,
                                center = Offset(rect.left, rect.top)
                            )
                            drawCircle(
                                color = handleBorderColor,
                                radius = handleRadius,
                                center = Offset(rect.left, rect.top),
                                style = Stroke(width = handleBorderWidth)
                            )
                            
                            // Top-right handle
                            drawCircle(
                                color = handleColor,
                                radius = handleRadius,
                                center = Offset(rect.right, rect.top)
                            )
                            drawCircle(
                                color = handleBorderColor,
                                radius = handleRadius,
                                center = Offset(rect.right, rect.top),
                                style = Stroke(width = handleBorderWidth)
                            )
                            
                            // Bottom-left handle
                            drawCircle(
                                color = handleColor,
                                radius = handleRadius,
                                center = Offset(rect.left, rect.bottom)
                            )
                            drawCircle(
                                color = handleBorderColor,
                                radius = handleRadius,
                                center = Offset(rect.left, rect.bottom),
                                style = Stroke(width = handleBorderWidth)
                            )
                            
                            // Bottom-right handle
                            drawCircle(
                                color = handleColor,
                                radius = handleRadius,
                                center = Offset(rect.right, rect.bottom)
                            )
                            drawCircle(
                                color = handleBorderColor,
                                radius = handleRadius,
                                center = Offset(rect.right, rect.bottom),
                                style = Stroke(width = handleBorderWidth)
                            )
                        }
                    }
                }
                
                // Controls at the bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(0.9f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Aspect ratio selection
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF333333).copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 8.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Free aspect ratio
                            FilterChip(
                                selected = selectedRatio == "FREE",
                                onClick = { selectedRatio = "FREE" },
                                label = { Text("Free") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2196F3),
                                    selectedLabelColor = Color.White
                                )
                            )
                            
                            // Square aspect ratio
                            FilterChip(
                                selected = selectedRatio == "SQUARE",
                                onClick = { selectedRatio = "SQUARE" },
                                label = { Text("1:1") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2196F3),
                                    selectedLabelColor = Color.White
                                )
                            )
                            
                            // 4:3 aspect ratio
                            FilterChip(
                                selected = selectedRatio == "4:3",
                                onClick = { selectedRatio = "4:3" },
                                label = { Text("4:3") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2196F3),
                                    selectedLabelColor = Color.White
                                )
                            )
                            
                            // 16:9 aspect ratio
                            FilterChip(
                                selected = selectedRatio == "16:9",
                                onClick = { selectedRatio = "16:9" },
                                label = { Text("16:9") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2196F3),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    
                    // Rotation controls
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF333333).copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 8.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rotate left button
                            IconButton(
                                onClick = {
                                    rotationAngle = (rotationAngle - 90f) % 360f
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2196F3)) // Material Blue
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.RotateLeft,
                                    contentDescription = "Rotate Left",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            // Current rotation indicator
                            Text(
                                text = "${rotationAngle.toInt()}°",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            // Rotate right button
                            IconButton(
                                onClick = {
                                    rotationAngle = (rotationAngle + 90f) % 360f
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2196F3)) // Material Blue
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.RotateRight,
                                    contentDescription = "Rotate Right",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Convert screen coordinates to bitmap coordinates
 */
private fun convertScreenRectToBitmapRect(
    screenRect: Rect,
    bitmapWidth: Int,
    bitmapHeight: Int,
    imageX: Float,
    imageY: Float,
    imageWidth: Float,
    imageHeight: Float
): android.graphics.Rect {
    // Calculate the scale factors
    val scaleX = bitmapWidth / imageWidth
    val scaleY = bitmapHeight / imageHeight
    
    // Convert screen coordinates to bitmap coordinates
    val left = ((screenRect.left - imageX) * scaleX).toInt().coerceIn(0, bitmapWidth)
    val top = ((screenRect.top - imageY) * scaleY).toInt().coerceIn(0, bitmapHeight)
    val right = ((screenRect.right - imageX) * scaleX).toInt().coerceIn(0, bitmapWidth)
    val bottom = ((screenRect.bottom - imageY) * scaleY).toInt().coerceIn(0, bitmapHeight)
    
    return android.graphics.Rect(left, top, right, bottom)
}

/**
 * Crop a bitmap using the specified rectangle
 */
private fun cropBitmap(bitmap: Bitmap, rect: android.graphics.Rect): Bitmap {
    val width = rect.width()
    val height = rect.height()
    
    // Ensure the crop rectangle is valid
    if (width <= 0 || height <= 0 || rect.left < 0 || rect.top < 0 || 
        rect.right > bitmap.width || rect.bottom > bitmap.height) {
        Logger.error("Invalid crop rectangle: $rect for bitmap size ${bitmap.width}x${bitmap.height}")
        return bitmap
    }
    
    return try {
        Bitmap.createBitmap(bitmap, rect.left, rect.top, width, height)
    } catch (e: Exception) {
        Logger.error("Error cropping bitmap: ${e.message}", e)
        bitmap
    }
}

/**
 * Load a bitmap from a URI
 */
private suspend fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            Logger.error("Error loading bitmap from URI: ${e.message}", e)
            null
        }
    }
}

/**
 * Rotate a bitmap by the specified angle
 */
private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    if (degrees == 0f) return bitmap
    
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

/**
 * Save a bitmap to a file and return its URI
 */
private fun saveBitmapToFile(context: Context, bitmap: Bitmap): Uri {
    val file = File(context.cacheDir, "processed_image_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return Uri.fromFile(file)
}
