rootProject.name = "crater"

// Backend
include(
    ":server-server"
)

// Shared
include(
    ":shared-data",
)

// Frontend
include(
    ":client-baseclient",
)


pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            name = "artifactory-menkalian"
            url = uri("https://artifactory.menkalian.de/artifactory/menkalian")
        }
    }
}