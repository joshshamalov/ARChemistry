package com.joshs.archemistry.feature_reactant

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

// Helper function to convert a content URI to a MultipartBody.Part
fun uriToMultipartBodyPart(contentResolver: ContentResolver, uri: Uri, partName: String): MultipartBody.Part? {
    return try {
        // Get input stream and determine MIME type
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val mimeType: String? = contentResolver.getType(uri)

        if (inputStream == null) {
            println("Error: Could not open InputStream for URI: $uri")
            return null
        }

        // Get filename (optional, but good practice for multipart)
        var fileName = "uploaded_image" // Default filename
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }

        // Read bytes from InputStream
        val fileBytes = inputStream.readBytes()
        inputStream.close() // Close the stream

        // Create RequestBody
        val requestBody: RequestBody = fileBytes.toRequestBody(
            mimeType?.toMediaTypeOrNull() // Use determined MIME type
                ?: "image/*".toMediaTypeOrNull() // Fallback MIME type
        )

        // Create MultipartBody.Part
        MultipartBody.Part.createFormData(partName, fileName, requestBody)

    } catch (e: Exception) {
        println("Error converting URI to MultipartBody.Part: ${e.message}")
        e.printStackTrace()
        null
    }
}