package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.fixtures.ConventionPlugin.Config
import dev.panuszewski.gradle.framework.BuildConfigurator
import dev.panuszewski.gradle.framework.Fixture
import dev.panuszewski.gradle.framework.GradleSpec

/**
 * The convention plugin with given name and body:
 * - defined in included build
 * - applied in the root project of the main build
 */
object ConventionPlugin : Fixture<Config> {

    override fun install(spec: GradleSpec, includedBuild: BuildConfigurator, config: Config) {
        spec.buildGradleKts {
            """
            plugins {
                id("${config.pluginName}")
            }
            
            repositories {
                mavenCentral()
            }
            """
        }

        spec.includedBuild {
            customProjectFile("src/main/kotlin/${config.pluginName}.gradle.kts") {
                config.pluginBody.trimIndent()
            }

            buildGradleKts {
                """
                plugins {
                    `kotlin-dsl`
                } 
                
                repositories {
                    gradlePluginPortal()
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
                    id("dev.panuszewski.typesafe-conventions") version "${spec.projectVersion}"
                }
                """
            }
        }
    }

    override fun defaultConfig() = Config()

    class Config {
        var pluginName = "some-convention"
        var pluginBody: String = ""
    }
}