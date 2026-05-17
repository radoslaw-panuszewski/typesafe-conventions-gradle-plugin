package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.fixtures.ConventionCatalogWithPluginInCustomLocation.Config
import dev.panuszewski.gradle.framework.Fixture
import dev.panuszewski.gradle.framework.GradleSpec

object ConventionCatalogWithPluginInCustomLocation : Fixture<Config> {

    override fun GradleSpec.install(config: Config) {
        buildGradleKts {
            """
            plugins {
                alias(conventions.plugins.someConvention)
            }
            
            repositories {
                mavenCentral()
            }
            """
        }

        includedBuild {
            customProjectFile("${config.sourceDirectory}/someConvention.gradle.kts") {
                """
                println("Hello from someConvention")    
                """
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
                        named("main") {
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
        var sourceDirectory: String? = null
    }
}
