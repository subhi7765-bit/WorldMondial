plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "sa.mondial.world.core.network"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Internal Architectural Layer Dependencies
    implementation(project(":core:common"))
    
    // Fixed Cleanly: Added database and domain module access for DTO entity mapping
    implementation(project(":core:database"))
    implementation(project(":core:domain"))

    // Type-Safe REST Remote Network Layer Client & Converters
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    // Fixed Cleanly: Added Timber framework dependency for TokenAuthenticator logging
    implementation(libs.timber)

    // Dagger-Hilt Dependency Injection Ecosystem with KSP Compiler
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
