plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.joshs.archemistry.feature_reactant" // Example namespace
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
        compose = true // Enable Compose for UI
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10" // Match app module
    }
}

dependencies {
    // Image Cropping
    implementation(libs.vanniktech.image.cropper)

// Image Loading (Coil)
    implementation(libs.coil.compose) // Assuming alias exists in libs.versions.toml
    // Other UI/Core dependencies (already present)
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
// Lifecycle ViewModel + Compose Integration
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Material Icons (for Button icons etc.)
    implementation(libs.androidx.compose.material.icons.extended)
// Activity Compose (for rememberLauncherForActivityResult)
    implementation(libs.androidx.activity.compose)

// Permissions Handling
    implementation(libs.accompanist.permissions)
// JSON Serialization (Gson)
    implementation(libs.google.gson)
    // Dependency on the core module (will be needed for utilities, network, etc.)
    implementation(project(":core"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}