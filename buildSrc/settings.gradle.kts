@file:Suppress("UnstableApiUsage")

rootProject.name = "buildSrc"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}

plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.5.1-SNAPSHOT"
}

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
    }
}