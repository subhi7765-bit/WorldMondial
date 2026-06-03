plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "sa.mondial.world.core.sync"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(project(":feature:news"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation(libs.timber)
}