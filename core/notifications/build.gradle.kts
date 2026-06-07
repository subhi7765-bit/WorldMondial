plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "sa.mondial.world.core.notifications"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    // Migrated from deprecated kotlinOptions block to modern compilerOptions DSL to enforce full Gradle 9.x compliance
    subprojects {
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Centralized Core Threading & Utility Layers
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:analytics"))

    // Kotlin Asynchronous Coroutines Core & Android Hooks
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Dagger-Hilt Dependency Injection Engine with KSP Processor
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Local Persistent Storage Cache Layers (Corrected version catalog naming references)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // AndroidX WorkManager, Logging, and Core Utilities
    implementation(libs.androidx.work.runtime)
    implementation(libs.timber)

    // Google Firebase Push Notifications Infrastructure
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
}
