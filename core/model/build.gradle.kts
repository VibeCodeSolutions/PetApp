plugins {
    id("tierapp.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.tierapp.core.model"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
}
