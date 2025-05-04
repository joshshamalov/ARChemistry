package com.joshs.archemistry.feature_reactant // Adjust package if needed

import android.content.ContentResolver // Import ContentResolver
import android.content.Context // Import Context
import android.net.Uri
import android.provider.OpenableColumns // Import OpenableColumns for filename
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshs.archemistry.core.network.ApiService // Import ApiService
import com.joshs.archemistry.core.network.RetrofitClient // Import RetrofitClient
import com.google.gson.Gson // Import Gson
import com.joshs.archemistry.core.network.ModelDataResponse
import com.joshs.archemistry.core.network.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel // Import Channel
import kotlinx.coroutines.flow.* // Import flow operators
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext // Import withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull // Import MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody // Import RequestBody creator
import java.io.InputStream // Import InputStream

// Data class to hold the screen state
data class ReactantScreenState(
    val croppedImageUri: Uri? = null,
    val availableReagents: List<String> = listOf("Hydrogenation", "Halogenation", "Dihydroxylation"), // Example, load from resources later
    val selectedReagent: String? = null,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val isProcessButtonEnabled: Boolean = false
)

class ReactantViewModel(
    // Remove context from constructor
    private val apiService: ApiService = RetrofitClient.apiService // Get service instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReactantScreenState())
    val uiState: StateFlow<ReactantScreenState> = _uiState.asStateFlow()

    // Channel for one-time navigation events
    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow() // Expose as Flow

    // --- Event Handlers ---

    fun onImageCropped(uri: Uri?) {
        _uiState.update { it.copy(croppedImageUri = uri) }
        updateProcessButtonState()
    }

    fun onReagentSelected(reagent: String?) {
         // Basic validation: ensure it's one of the available reagents or null
        val validReagent = if (reagent in _uiState.value.availableReagents) reagent else null
        _uiState.update { it.copy(selectedReagent = validReagent) }
        updateProcessButtonState()
    }

    // Accept ContentResolver as a parameter here
    fun onProcessReactionClicked(contentResolver: ContentResolver) {
        if (!_uiState.value.isProcessButtonEnabled) return // Safety check

        val imageUri = _uiState.value.croppedImageUri ?: return // Ensure URI exists
        val selectedReagent = _uiState.value.selectedReagent // Get selected reagent name
        if (selectedReagent == null) {
             _uiState.update { it.copy(errorMessage = "Reagent not selected") }
             return // Should be prevented by button state, but safety check
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            var errorMsg: String? = null
            // Store the received model data (or null if error)
            var receivedModelData: ModelDataResponse?

            try {
                // 1. Get image bytes and create MultipartBody.Part, passing the resolver
                // Pass the resolver to the helper function
                val imagePart = uriToMultipartBodyPart(contentResolver, imageUri, "image")

                if (imagePart == null) {
                    errorMsg = "Failed to read image file."
                } else {
                    // 2. Call API
                    // Switch to IO dispatcher for network call
                    // Create RequestBody for the reagent name
                    val reagentNamePart = selectedReagent.toRequestBody()

                    // 2. Call API with image and reagent name
                    val response = withContext(Dispatchers.IO) {
                        apiService.processImage(imagePart, reagentNamePart)
                    }

                    // 3. Handle response
                    if (response.isSuccessful) {
                        val modelDataResponse = response.body()
                        // Check if the body is not null AND the error field within the body is null
                        if (modelDataResponse != null && modelDataResponse.error == null) {
                            // Validate that we received the expected data
                            if (modelDataResponse.atoms != null && modelDataResponse.coords != null && modelDataResponse.bonds != null) {
                                receivedModelData = modelDataResponse // Store the successful data
                                println("Received 3D Model Data: ${receivedModelData.atoms!!.size} atoms") // Log success
                                // Serialize data to JSON
                                val modelDataJson = Gson().toJson(receivedModelData)
                                // Emit navigation event
                                _navigationEvent.send(NavigationEvent.NavigateToArProduct(modelDataJson))
                            } else {
                                // Successful response code, but missing data in the body
                                errorMsg = "Server returned incomplete model data."
                            }
                        } else {
                            // Server processed but returned an error message in the body (e.g., Imago/RDKit/Reaction failed)
                            errorMsg = modelDataResponse?.error ?: "Server failed to process image/reaction."
                        }
                    } else {
                        // Network error or non-2xx HTTP status
                        errorMsg = "Server error: ${response.code()} ${response.message()}"
                        // Optionally parse error body: response.errorBody()?.string()
                    }
                }
            } catch (e: Exception) {
                // Catch other exceptions (network, IO, etc.)
                println("Error processing reaction: ${e.message}")
                e.printStackTrace()
                errorMsg = "An error occurred: ${e.localizedMessage}"
            } finally {
                // 8. Update UI state
                _uiState.update { it.copy(isProcessing = false, errorMessage = errorMsg) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // --- Private Helper ---

    private fun updateProcessButtonState() {
        val imageSelected = _uiState.value.croppedImageUri != null
        val reagentSelected = _uiState.value.selectedReagent != null
        _uiState.update { it.copy(isProcessButtonEnabled = imageSelected && reagentSelected) }
    }
}

// Sealed class for navigation events
sealed class NavigationEvent {
    data class NavigateToArProduct(val modelDataJson: String) : NavigationEvent()
    // Add other navigation events here if needed
}