@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    alias(libs.plugins.plugin.publish)
    alias(libs.plugins.axion.release)
}

scmVersion {
    unshallowRepoOnCI = true
}

group = "dev.panuszewski"
version = scmVersion.version

gradlePlugin {
    website = "https://github.com/radoslaw-panuszewski/typesafe-conventions-gradle-plugin"
    vcsUrl = "https://github.com/radoslaw-panuszewski/typesafe-conventions-gradle-plugin"

    plugins {
        create("typesafeConventions") {
            id = "dev.panuszewski.typesafe-conventions"
            implementationClass = "dev.panuszewski.gradle.TypesafeConventionsPlugin"
            displayName = "Typesafe Conventions Plugin"
            description = "Gradle plugin providing typesafe accessors for convention plugins"
            tags = listOf("build-logic", "buildSrc")
        }
    }
}

tasks {
    named("publishPlugins") {
        notCompatibleWithConfigurationCache("Uses Task.project at execution time")
    }
}