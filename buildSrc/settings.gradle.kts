@file:Suppress("UnstableApiUsage")

rootProject.name = "buildSrc"

plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.9.1"
}

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
    }
}