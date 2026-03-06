plugins {
    id("tierapp.android.library")
    id("tierapp.hilt")
}

android {
    namespace = "com.example.tierapp.core.media"
}

dependencies {
    implementation(project(":core:model"))
}
