plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.joshs.archemistry"
    compileSdk = 35 // Updated to align with AGP 8.4.1

    defaultConfig {
        applicationId = "com.joshs.archemistry"
        minSdk = 24 // As specified in README (Line 16)
        targetSdk = 35 // Updated to align with AGP 8.4.1
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10" // Compatible with Kotlin 1.9.22
    }
}

dependencies {

    // Core Android & Compose dependencies (already present)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

// Material Components (for legacy themes used by libraries like image cropper)
    implementation(libs.google.android.material)
    // Project Module Dependencies
    implementation(project(":core"))
    implementation(project(":feature_reactant"))
    implementation(project(":feature_ar_product"))
    // implementation(project(":feature_backend_vis")) // Removed unused module dependency
    implementation(project(":lib_chemistry"))
    implementation(project(":lib_testing")) // If app needs direct access to test utils

    // Navigation (App module likely handles the NavHost)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
// Removed resolutionStrategy as AGP 8.4.1 should handle dependencies correctly with compileSdk 35