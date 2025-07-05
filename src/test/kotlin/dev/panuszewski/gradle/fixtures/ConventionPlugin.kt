package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.fixtures.ConventionPlugin.Config
import dev.panuszewski.gradle.framework.Fixture
import dev.panuszewski.gradle.framework.GradleSpec

/**
 * The convention plugin with given name and body:
 * - defined in included build
 * - applied in the root project of the main build
 */
object ConventionPlugin : Fixture<Config> {

    override fun GradleSpec.install(config: Config) {
        buildGradleKts {
            """
            plugins {
                id("${config.pluginName}")
            }
            
            repositories {
                mavenCentral()
            }
            """
        }

        includedBuild {
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
        }
    }

    override fun defaultConfig() = Config()

    class Config {
        var pluginName = "some-convention"
        var pluginBody: String = ""
    }
}