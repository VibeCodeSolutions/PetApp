plugins {
    id("tierapp.android.library")
}

android {
    namespace = "com.example.tierapp.core.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
