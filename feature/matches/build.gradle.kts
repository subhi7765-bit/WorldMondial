plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "sa.mondial.world.feature.matches"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Internal Architectural Layer Dependencies
    implementation(project(":core:common"))
    implementation(project(":core:di"))
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:notifications"))
    implementation(project(":core:analytics"))

    // Native Jetpack Compose Core Framework
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    // Jetpack Pagination 3 Integration
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // Dagger-Hilt Dependency Injection Engine
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Unidirectional Architecture Lifecycle & Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Serialization & Specialized Presentation Shimmer Components
    implementation(libs.kotlinx.serialization.json)
    implementation("com.valentinilk.shimmer:compose-shimmer:1.3.1")

    // Unified Test Pipeline Hooks
    testImplementation(project(":core:testing"))
}
