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

        testInstrumentationRunner = "sa.mondial.world.core.testing.CustomTestRunner"
        consumerProguardFiles("consumer-rules.pro")

        // Read API keys and Base URLs directly from GitHub Actions Environment Variables
        val footballApiKey = System.getenv("FOOTBALL_API_KEY") ?: "MISSING_KEY"
        val newsApiKey = System.getenv("NEWS_API_KEY") ?: "MISSING_KEY"
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
    
    // THE FIX: Connecting Network module to Database module so it can recognize Entities
    implementation(project(":core:database"))

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.logging)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.timber)
}
