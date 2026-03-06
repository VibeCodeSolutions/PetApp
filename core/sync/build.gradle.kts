plugins {
    id("tierapp.android.library")
    id("tierapp.hilt")
}

android {
    namespace = "com.example.tierapp.core.sync"
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)
    implementation(libs.kotlinx.coroutines.android)
}
