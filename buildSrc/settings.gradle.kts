@file:Suppress("UnstableApiUsage")

rootProject.name = "buildSrc"

plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.5.0"
}

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
    }
}