pluginManagement {
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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Rat Android SDK Example"

// Include the SDK from the sibling project
includeBuild("../rat-android-sdk") {
    dependencySubstitution {
        substitute(module("com.example:rat-ads")).using(project(":rat-ads"))
    }
}

include(":app")
