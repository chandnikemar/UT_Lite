pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://jitpack.io")
        maven(url = "https://zebratech.jfrog.io/artifactory/EMDK-Android/")
        jcenter()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://zebratech.jfrog.io/artifactory/EMDK-Android/")
        jcenter()
    }
}

rootProject.name = "UtLite"
include(":app")
 