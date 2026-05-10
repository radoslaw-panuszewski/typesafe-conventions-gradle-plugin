@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://central.sonatype.com/repository/maven-snapshots")
    }
}

plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.10.2-convention-catalogs-SNAPSHOT"
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
