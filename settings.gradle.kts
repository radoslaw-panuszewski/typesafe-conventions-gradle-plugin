@file:Suppress("UnstableApiUsage")

plugins {
    id("com.gradle.develocity") version "4.0.1"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "typesafe-conventions-gradle-plugin"
