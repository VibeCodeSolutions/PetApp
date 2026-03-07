plugins {
    id("tierapp.android.library")
    id("tierapp.hilt")
}

android {
    namespace = "com.example.tierapp.core.sync"

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:model"))
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)
    implementation(libs.kotlinx.coroutines.android)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
}
