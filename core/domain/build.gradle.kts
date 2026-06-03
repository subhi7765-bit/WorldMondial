plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "sa.mondial.world.core.domain"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
