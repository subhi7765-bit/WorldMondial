import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

// Load local.properties safely for CI/CD injection
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "sa.mondial.world.core.network"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "sa.mondial.world.core.testing.CustomTestRunner"
        consumerProguardFiles("consumer-rules.pro")

        // Read API keys safely from Environment Variables OR injected local.properties
        val footballApiKey = System.getenv("FOOTBALL_API_KEY") ?: localProperties.getProperty("FOOTBALL_API_KEY") ?: "MISSING_KEY"
        val newsApiKey = System.getenv("NEWS_API_KEY") ?: localProperties.getProperty("NEWS_API_KEY") ?: "MISSING_KEY"
        
        val footballBaseUrl = "https://api.football-data.org/v4/"
        val newsBaseUrl = "https://newsapi.org/v2/"

        // Inject variables into BuildConfig securely
        buildConfigField("String", "FOOTBALL_API_KEY", "\"$footballApiKey\"")
        buildConfigField("String", "NEWS_API_KEY", "\"$newsApiKey\"")
        buildConfigField("String", "FOOTBALL_BASE_URL", "\"$footballBaseUrl\"")
        buildConfigField("String", "NEWS_BASE_URL", "\"$newsBaseUrl\"")
    }

    buildFeatures {
        buildConfig = true
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
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.logging)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.timber)
}
