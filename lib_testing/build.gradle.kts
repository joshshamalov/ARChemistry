plugins {
    id("org.jetbrains.kotlin.jvm") // Pure Kotlin library
}

java {
    sourceCompatibility = JavaVersion.VERSION_17 // Match project Java version
    targetCompatibility = JavaVersion.VERSION_17 // Match project Java version
}

dependencies {
    // Testing utility dependencies (e.g., JUnit, Mockito, sample data access) can be added here
    implementation(libs.kotlin.stdlib) // Example dependency

    // Include JUnit API for writing tests within this library if needed
    api(libs.junit)
}