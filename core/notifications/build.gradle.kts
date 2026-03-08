plugins {
    id("tierapp.android.library")
    id("tierapp.hilt")
}

android {
    namespace = "com.example.tierapp.core.notifications"
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:model"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging.ktx)
}
