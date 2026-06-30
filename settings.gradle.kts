@file:Suppress("UnstableApiUsage")

plugins {
    id("com.gradle.develocity") version "4.5.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "typesafe-conventions-gradle-plugin"

includeBuild("build-logic")
