pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://www.jitpack.io")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        flatDir {
            dirs("app/libs")  // ✅ 让 Gradle 识别 `libs/` 目录
        }
    }
}

rootProject.name = "OpperDemo"
include(":app")
