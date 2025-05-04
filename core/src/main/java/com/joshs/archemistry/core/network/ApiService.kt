package com.joshs.archemistry.core.network // Example package in core module

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response // Use Retrofit Response for detailed status/error handling
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody // Import the correct extension

// Data class for individual molecule structure (reactant or product)
data class MoleculeStructureData(
    val atoms: List<String>? = null, // List of atom element symbols (e.g., "C", "H")
    val coords: List<List<Double>>? = null, // List of [x, y, z] coordinates
    val bonds: List<List<Int>>? = null, // List of [atomIdx1, atomIdx2, bondOrder]
    val error: String? = null // Error message if processing failed
)

// Data class for the nested response from the server
data class ReactionResponse(
    val reactant: MoleculeStructureData? = null,
    val product: MoleculeStructureData? = null,
    val error: String? = null // Overall error message
)

interface ApiService {

    @Multipart
    @POST("/process_image") // Endpoint defined in Flask app.py
    suspend fun processImage(
        @Part image: MultipartBody.Part, // The image file itself
        @Part("reagent_name") reagentName: RequestBody // The selected reagent name
    ): Response<ReactionResponse> // Updated to use the nested response type
}

// Helper function (optional, could be in Repository) to create RequestBody from String
// Revert to deprecated version to fix build error, address warning later
fun String.toRequestBody(): RequestBody =
    this.toRequestBody("multipart/form-data".toMediaTypeOrNull())