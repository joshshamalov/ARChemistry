package com.joshs.archemistry.core.network

import com.joshs.archemistry.core.BuildConfig // Assuming BuildConfig exists in core
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // IMPORTANT: Replace with your actual desktop IP address
    // Make sure your phone/emulator and desktop are on the same network
    private const val BASE_URL = "http://192.168.1.175:5000/" // Use your server's IP and port

    // Lazy initialization of OkHttpClient
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Increase timeouts if needed
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .apply {
                // Add logging interceptor only for debug builds
                if (BuildConfig.DEBUG) { // Check build type
                    val loggingInterceptor = HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY // Log request/response body
                    }
                    addInterceptor(loggingInterceptor)
                }
            }
            .build()
    }

    // Lazy initialization of Retrofit instance
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Use the configured OkHttpClient
            .addConverterFactory(GsonConverterFactory.create()) // Use Gson for JSON parsing
            .build()
    }

    // Lazy initialization of the ApiService implementation
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}