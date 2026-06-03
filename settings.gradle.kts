pluginManagement {
    repositories {
        google()
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

// Modules partition defining modern strict Clean Architecture boundaries
include(":app")
include(":core:common")
include(":core:di")
include(":core:network")
include(":core:database")
include(":core:data")
include(":core:domain")
include(":core:ui")
include(":core:testing")
include(":core:notifications")
include(":core:analytics")
include(":feature:matches")
include(":feature:news")
include(":feature:settings")
include(":core:sync")