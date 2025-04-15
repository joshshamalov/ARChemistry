plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.joshs.archemistry"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.joshs.archemistry"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    ndkVersion = "25.2.9519653"
    buildToolsVersion = "35.0.0"
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Material Components (required for Image Cropper)
    implementation("com.google.android.material:material:1.11.0")

    // AppCompat (required for UCrop)
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // JGraphT - Graph library for molecular structure representation
    implementation("org.jgrapht:jgrapht-core:1.5.2")
    implementation("org.jgrapht:jgrapht-io:1.5.2")

    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended:1.6.3")

    // Google AR Core
    implementation("com.google.ar:core:1.48.0")

    // OpenCV for image processing - commented out for now
    // implementation(project(":opencv"))

    // Android Image Cropper library
    implementation("com.vanniktech:android-image-cropper:4.6.0")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}