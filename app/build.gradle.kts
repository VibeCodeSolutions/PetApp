plugins {
    id("tierapp.android.app")
    id("tierapp.compose")
    id("tierapp.hilt")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.example.tierapp"
    defaultConfig {
        applicationId = "com.example.tierapp"
        versionCode = 1
        versionName = "1.0"
    }
    signingConfigs {
        create("release") {
            storeFile = System.getenv("KEYSTORE_PATH")?.let { file(it) }
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:sync"))
    implementation(project(":feature:pets"))
    implementation(project(":feature:family"))
    implementation(project(":feature:gallery"))
    implementation(project(":feature:health"))
    implementation(project(":feature:settings"))
    implementation(libs.coil.compose)
    implementation(project(":core:media"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.compose.material.icons.extended)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.crashlytics)

    // Google Sign-In via Credential Manager (ersetzt GoogleSignInClient)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // WorkManager + Hilt-Worker-Integration (für HiltWorkerFactory in TierappApplication)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.profileinstaller)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
}
