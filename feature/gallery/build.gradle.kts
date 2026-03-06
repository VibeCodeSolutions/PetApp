plugins {
    id("tierapp.android.library")
    id("tierapp.compose")
    id("tierapp.hilt")
}

android {
    namespace = "com.example.tierapp.feature.gallery"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:media"))
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.navigation.compose)
}
