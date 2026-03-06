plugins {
    id("tierapp.android.library")
    id("tierapp.compose")
    id("tierapp.hilt")
}

android {
    namespace = "com.example.tierapp.feature.pets"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:common"))
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.navigation.compose)
}
