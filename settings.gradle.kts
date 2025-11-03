@file:Suppress("UnstableApiUsage")

plugins {
    id("com.gradle.develocity") version "4.2.2"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "typesafe-conventions-gradle-plugin"
