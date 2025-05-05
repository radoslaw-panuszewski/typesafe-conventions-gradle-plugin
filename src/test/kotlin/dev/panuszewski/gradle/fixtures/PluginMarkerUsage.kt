package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.BuildConfigurator
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object PluginMarkerUsage : NoConfigFixture {

    val somePlugin = "pl.allegro.tech.build.axion-release"
    val somePluginVersion = "1.18.16"
    val taskRegisteredBySomePlugin = "verifyRelease"

    override fun install(spec: GradleSpec, includedBuild: BuildConfigurator, config: Unit) {
        with (spec) {
            libsVersionsToml {
                """
                [plugins]
                some-plugin = { id = "$somePlugin", version = "$somePluginVersion" }
                """
            }

            buildGradleKts {
                """
                plugins {
                    id("some-convention")
                }
                
                repositories {
                    mavenCentral()
                }
                """
            }

            includedBuild {
                buildGradleKts {
                    """
                    import dev.panuszewski.gradle.pluginMarker
                        
                    plugins {
                        `kotlin-dsl`
                    } 
                    
                    repositories {
                        mavenCentral()
                    }
                    
                    dependencies {
                        implementation(pluginMarker(libs.plugins.some.plugin))
                    }
                    """
                }

                settingsGradleKts {
                    """
                    pluginManagement {
                        repositories {
                            gradlePluginPortal()
                            mavenLocal()
                        }
                    }
                        
                    plugins {
                        id("dev.panuszewski.typesafe-conventions") version "$projectVersion"
                    }
                    """
                }

                customProjectFile("src/main/kotlin/some-convention.gradle.kts") {
                    """
                    plugins {
                        id("$somePlugin")
                    }
                    """
                }
            }
        }
    }
}