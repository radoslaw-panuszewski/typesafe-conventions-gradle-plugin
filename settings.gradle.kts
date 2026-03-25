@file:Suppress("UnstableApiUsage")

plugins {
    id("com.gradle.develocity") version "4.4.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "typesafe-conventions-gradle-plugin"
