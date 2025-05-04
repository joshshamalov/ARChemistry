plugins {
    id("org.jetbrains.kotlin.jvm") // Pure Kotlin library
}

java {
    sourceCompatibility = JavaVersion.VERSION_17 // Match project Java version
    targetCompatibility = JavaVersion.VERSION_17 // Match project Java version
}

dependencies {
    // Chemistry Development Kit (CDK) - Non-AWT modules ONLY
    implementation(libs.cdk.core)
    implementation(libs.cdk.smiles)
    implementation(libs.cdk.sdg) // For 2D/3D coordinate generation
    // Add other non-AWT CDK modules (e.g., cdk-structgen, cdk-forcefield, cdk-io) here if needed later

    implementation(libs.kotlin.stdlib) // Ensure stdlib is present

    testImplementation(libs.junit)
}