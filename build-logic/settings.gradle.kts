@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.12.2"
}

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "build-logic"

typesafeConventions {
    conventionCatalog {
        ignorePackageNames = true
    }
}
