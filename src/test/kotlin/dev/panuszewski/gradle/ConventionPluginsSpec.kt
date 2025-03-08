package dev.panuszewski.gradle

import dev.panuszewski.gradle.util.BaseGradleSpec
import dev.panuszewski.gradle.util.BuildOutcome.BUILD_FAILED
import dev.panuszewski.gradle.util.BuildOutcome.BUILD_SUCCESSFUL
import dev.panuszewski.gradle.util.IncludedBuildConfigurator
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ConventionPluginsSpec : BaseGradleSpec() {

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should allow to use catalog accessors in convention plugin`(
        includedBuildForConventionPlugins: IncludedBuildConfigurator
    ) {
        // given
        val someLibrary = "org.apache.commons:commons-lang3:3.17.0"

        customProjectFile("gradle/libs.versions.toml") {
            """
            [libraries]
            some-library = "$someLibrary"
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

        includedBuildForConventionPlugins {
            buildGradleKts {
                """
                plugins {
                    `kotlin-dsl`
                } 
                
                repositories {
                    mavenCentral()
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
                    id("dev.panuszewski.typesafe-conventions") version "${System.getenv("PROJECT_VERSION")}"
                }
                """
            }

            customProjectFile("src/main/kotlin/some-convention.gradle.kts") {
                """
                plugins {
                    java
                }
                
                dependencies {
                    implementation(libs.some.library)
                }
                """
            }
        }

        // when
        val result = runGradle("dependencyInsight", "--dependency", someLibrary)

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain someLibrary
        result.output shouldNotContain "$someLibrary FAILED"
    }

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should allow to use catalog accessors in plugins block of convention plugin`(
        includedBuildForConventionPlugins: IncludedBuildConfigurator
    ) {
        // given
        val somePlugin = "pl.allegro.tech.build.axion-release"
        val somePluginVersion = "1.18.16"
        val taskRegisteredBySomePlugin = "verifyRelease"

        customProjectFile("gradle/libs.versions.toml") {
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

        includedBuildForConventionPlugins {
            buildGradleKts {
                """
                plugins {
                    `kotlin-dsl`
                } 
                
                repositories {
                    mavenCentral()
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
                    id("dev.panuszewski.typesafe-conventions") version "${System.getenv("PROJECT_VERSION")}"
                }
                """
            }

            customProjectFile("src/main/kotlin/some-convention.gradle.kts") {
                """
                plugins {
                    alias(libs.plugins.some.plugin)
                }
                """
            }
        }

        // when
        val result = runGradle("tasks")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain taskRegisteredBySomePlugin
    }

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should respect disabling accessors in plugins block`(
        includedBuildForConventionPlugins: IncludedBuildConfigurator
    ) {
        // given
        val somePlugin = "pl.allegro.tech.build.axion-release"
        val somePluginVersion = "1.18.16"

        customProjectFile("gradle/libs.versions.toml") {
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

        includedBuildForConventionPlugins {
            buildGradleKts {
                """
                plugins {
                    `kotlin-dsl`
                } 
                
                repositories {
                    mavenCentral()
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
                    id("dev.panuszewski.typesafe-conventions") version "${System.getenv("PROJECT_VERSION")}"
                }
                
                typesafeConventions {
                    accessorsInPluginsBlock = false
                }
                """
            }

            customProjectFile("src/main/kotlin/some-convention.gradle.kts") {
                """
                plugins {
                    alias(libs.plugins.some.plugin)
                }
                """
            }
        }

        // when
        val result = runGradle("help")

        // then
        result.buildOutcome shouldBe BUILD_FAILED
        result.output shouldContain "Unresolved reference: libs"
    }

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should respect disabling auto plugin dependencies`(
        includedBuildForConventionPlugins: IncludedBuildConfigurator
    ) {
        // given
        val somePlugin = "pl.allegro.tech.build.axion-release"
        val somePluginVersion = "1.18.16"

        customProjectFile("gradle/libs.versions.toml") {
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

        includedBuildForConventionPlugins {
            buildGradleKts {
                """
                plugins {
                    `kotlin-dsl`
                } 
                
                repositories {
                    mavenCentral()
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
                    id("dev.panuszewski.typesafe-conventions") version "${System.getenv("PROJECT_VERSION")}"
                }
                
                typesafeConventions {
                    autoPluginDependencies = false
                }
                """
            }

            customProjectFile("src/main/kotlin/some-convention.gradle.kts") {
                """
                plugins {
                    alias(libs.plugins.some.plugin)
                }
                """
            }
        }

        // when
        val result = runGradle("help")

        // then
        result.buildOutcome shouldBe BUILD_FAILED
        result.output shouldContain "Plugin [id: '$somePlugin'] was not found in any of the following sources"
    }
}