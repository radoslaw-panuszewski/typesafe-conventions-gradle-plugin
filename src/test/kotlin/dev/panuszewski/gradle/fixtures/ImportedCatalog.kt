package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.conventionPluginAppliedInRootProject
import dev.panuszewski.gradle.util.GradleSpec
import dev.panuszewski.gradle.util.BuildConfigurator

object ImportedCatalog : Fixture {

    const val catalogCoordinates = "io.micronaut.platform:micronaut-platform:4.8.2"
    const val libraryFromCatalog = "io.micronaut:micronaut-core:4.8.11"

    override fun install(spec: GradleSpec, includedBuild: BuildConfigurator): ImportedCatalog {
        spec.settingsGradleKts {
            append {
                """
                dependencyResolutionManagement {
                    repositories {
                        mavenCentral()
                    }
                
                    versionCatalogs {
                        create("mn") {
                            from("$catalogCoordinates")
                        }
                    }
                }
                """
            }
        }

        spec.conventionPluginAppliedInRootProject(includedBuild) {
            """
            plugins {
                java
            }
            
            dependencies {
                implementation(mn.micronaut.core)
            }
            """
        }

        return this
    }
}