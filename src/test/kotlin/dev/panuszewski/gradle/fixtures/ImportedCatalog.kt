package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.conventionPluginAppliedInRootProject

class ImportedCatalog : TestFixture() {

    val catalogCoordinates = "io.micronaut.platform:micronaut-platform:4.8.2"
    val libraryFromCatalog = "io.micronaut:micronaut-core:4.8.11"

    override fun installFixture(): ImportedCatalog {
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