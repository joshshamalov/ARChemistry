plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.joshs.archemistry.core" // Example namespace, adjust if needed
    compileSdk = 35 // Match app module

    defaultConfig {
        minSdk = 24 // Match app module
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // Match app module
        targetCompatibility = JavaVersion.VERSION_17 // Match app module
    }
    kotlinOptions {
        jvmTarget = "17" // Match app module
    }
buildFeatures {
        buildConfig = true // Enable BuildConfig generation for library module
    }
}

dependencies {
    // Networking
    api(libs.squareup.retrofit) // Use 'api' to expose Retrofit to dependent modules
    implementation(libs.squareup.retrofit.converter.gson)
    implementation(libs.squareup.okhttp.logging) // Use implementation for debug builds only if needed via build variants

    // Other core utilities (already present)
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}