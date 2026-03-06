plugins {
    id("tierapp.android.library")
    id("tierapp.compose")
    id("tierapp.hilt")
}

android {
    namespace = "com.example.tierapp.core.media"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
