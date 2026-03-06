pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Tierapp"

include(":app")

// Core modules
include(":core:model")
include(":core:database")
include(":core:network")
include(":core:sync")
include(":core:common")
include(":core:ui")
include(":core:notifications")
include(":core:media")

// Feature modules
include(":feature:pets")
include(":feature:health")
include(":feature:gallery")
include(":feature:family")
include(":feature:settings")
