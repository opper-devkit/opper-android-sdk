// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.8.0" apply false
}

allprojects {
    repositories {
        flatDir {
            dirs("app/libs")  // ✅ 让 Gradle 识别 `libs/` 目录
        }
    }
}
