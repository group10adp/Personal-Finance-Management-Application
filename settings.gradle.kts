pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")  // For Android-related plugins
                includeGroupByRegex("com\\.google.*")   // For Google plugins
                includeGroupByRegex("androidx.*")       // For AndroidX plugins
            }
        }
        mavenCentral()      // Standard repository for open-source libraries
        gradlePluginPortal() // Repository for Gradle plugins
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // Fail if project-specific repositories are used
    repositories {
        google()            // Google's Maven repository
        mavenCentral()      // Maven Central repository
        maven { url = uri("https://jitpack.io") } // Custom JitPack repository
    }
}

rootProject.name = "Finance Manager"
include(":app")  // Include the app module in the project
