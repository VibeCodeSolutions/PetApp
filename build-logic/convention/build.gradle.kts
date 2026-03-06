import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.example.tierapp.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApp") {
            id = "tierapp.android.app"
            implementationClass = "AndroidAppConventionPlugin"
        }
        register("androidLibrary") {
            id = "tierapp.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("compose") {
            id = "tierapp.compose"
            implementationClass = "ComposeConventionPlugin"
        }
        register("hilt") {
            id = "tierapp.hilt"
            implementationClass = "HiltConventionPlugin"
        }
    }
}
