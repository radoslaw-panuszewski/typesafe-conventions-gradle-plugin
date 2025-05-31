@file:Suppress("UnstableApiUsage")

rootProject.name = "buildSrc"

plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.7.3"
}

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
    }
}