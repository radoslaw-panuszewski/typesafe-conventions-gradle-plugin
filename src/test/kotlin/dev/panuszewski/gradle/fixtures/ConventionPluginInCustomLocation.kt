package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.fixtures.ConventionPluginInCustomLocation.Config
import dev.panuszewski.gradle.framework.Fixture
import dev.panuszewski.gradle.framework.GradleSpec

/**
 * Similar to [ConventionPluginApplied], but with custom source directory for convention plugins
 */
object ConventionPluginInCustomLocation : Fixture<Config> {

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
            customProjectFile("${config.sourceDirectory}/${config.pluginName}.gradle.kts") {
                config.pluginBody.trimIndent()
            }

            buildGradleKts {
                """
                plugins {
                    `kotlin-dsl-base`
                }
                
                repositories {
                    gradlePluginPortal()
                }

                kotlin {
                    sourceSets {
                        named("${config.sourceSet}") {
                            kotlin.srcDir("${config.sourceDirectory}")
                        }
                    }
                }

                apply(plugin = "org.gradle.kotlin.kotlin-dsl")
                """
            }
        }
    }

    override fun defaultConfig() = Config()

    class Config {
        var pluginName: String = "some-convention"
        var pluginBody: String = ""
        var sourceSet: String? = null
        var sourceDirectory: String? = null
    }
}
