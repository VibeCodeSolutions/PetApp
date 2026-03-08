plugins {
    id("tierapp.android.library")
    id("tierapp.hilt")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.tierapp.core.database"
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)
    implementation(libs.kotlinx.coroutines.android)

    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.junit.ext)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
