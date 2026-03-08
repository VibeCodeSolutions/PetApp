plugins {
    id("tierapp.android.library")
    id("tierapp.compose")
    id("tierapp.hilt")
}

android {
    namespace = "com.example.tierapp.feature.health"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.compose.material.icons.extended)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
}
