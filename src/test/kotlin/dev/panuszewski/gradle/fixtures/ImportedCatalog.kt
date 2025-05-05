package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.BuildConfigurator
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object ImportedCatalog : NoConfigFixture {

    const val catalogCoordinates = "io.micronaut.platform:micronaut-platform:4.8.2"
    const val libraryFromCatalog = "io.micronaut:micronaut-core:4.8.11"

    override fun install(spec: GradleSpec, includedBuild: BuildConfigurator, config: Unit) {
        with (spec) {
            settingsGradleKts {
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

            installFixture(ConventionPlugin) {
                pluginBody = """
                    plugins {
                        java
                    }
                    
                    dependencies {
                        implementation(mn.micronaut.core)
                    }
                    """
            }
        }
    }
}