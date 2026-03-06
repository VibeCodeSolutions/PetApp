plugins {
    id("tierapp.android.library")
    id("tierapp.compose")
}

android {
    namespace = "com.example.tierapp.core.ui"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.coil.compose)
}
