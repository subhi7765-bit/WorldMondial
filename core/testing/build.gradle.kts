plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "sa.mondial.world.core.testing"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
}

dependencies {
    api(libs.junit)
    api(libs.mockk)
    api(libs.turbine)
    api(libs.kotlinx.coroutines.test)
    api(libs.androidx.test.rules)
}