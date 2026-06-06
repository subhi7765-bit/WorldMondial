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

rootProject.name = "WorldMondial"

// ==================== Core Layers Modules ====================
include(":core:common")
include(":core:di")
include(":core:network")
include(":core:database")
include(":core:data")
include(":core:domain")
include(":core:ui")
include(":core:sync")
include(":core:testing")
include(":core:notifications")
include(":core:analytics")

// ==================== Feature UI Modules ====================
include(":feature:matches")
include(":feature:news")
include(":feature:settings")

// ==================== Main App Module ====================
include(":app")
