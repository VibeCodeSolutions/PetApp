plugins {
    id("tierapp.android.library")
    id("tierapp.hilt")
}

android {
    namespace = "com.example.tierapp.core.notifications"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging.ktx)
}
