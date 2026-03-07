plugins {
    id("tierapp.android.library")
    id("tierapp.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.tierapp.core.network"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.bundles.network)
    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
}
