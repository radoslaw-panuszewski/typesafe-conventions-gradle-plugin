package dev.panuszewski.gradle

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.BuildOutcome.BUILD_SUCCESSFUL
import dev.panuszewski.gradle.framework.BuildConfigurator
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.params.ParameterizedTest

class IncludedBuildSpec : GradleSpec() {

    @ParameterizedTest
    @AllIncludedBuildTypes
    fun `should allow to use catalog accessors in included build`(includedBuild: BuildConfigurator) {
        // given
        val someLibrary = "org.apache.commons:commons-lang3:3.17.0"

        libsVersionsToml {
            """
            [libraries]
            some-library = "$someLibrary"
            """
        }

        buildGradleKts {
            """
            plugins {
                java
            }
            """
        }

        includedBuild {
            buildGradleKts {
                """
                plugins {
                    `kotlin-dsl`
                } 
                
                repositories {
                    mavenCentral()
                }
                
                dependencies {
                    implementation(libs.some.library)
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
        }

        // when
        val result = runGradle("assemble")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
    }

    @ParameterizedTest
    @AllIncludedBuildTypes
    fun `should allow to use catalog accessors in included build when running task from subproject`(includedBuild: BuildConfigurator) {
        // given
        val someLibrary = "org.apache.commons:commons-lang3:3.17.0"

        libsVersionsToml {
            """
            [libraries]
            some-library = "$someLibrary"
            """
        }

        subprojectBuildGradleKts("subproject") {
            """
            plugins {
                java
            }
            """
        }

        includedBuild {
            buildGradleKts {
                """
                plugins {
                    `kotlin-dsl`
                }
                    
                repositories {
                    mavenCentral()
                }
                
                dependencies {
                    implementation(libs.some.library)
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
        }

        // when
        val result = runGradle(":subproject:assemble")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
    }

    @ParameterizedTest
    @AllIncludedBuildTypes
    fun `should provide pluginMarker helper method`(includedBuild: BuildConfigurator) {
        // given
        val somePlugin = "pl.allegro.tech.build.axion-release"
        val somePluginVersion = "1.18.16"
        val taskRegisteredBySomePlugin = "verifyRelease"

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

        // when
        val result = runGradle("tasks")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain taskRegisteredBySomePlugin
    }

    @ParameterizedTest
    @AllIncludedBuildTypes
    fun `should not prevent applying kotlin plugin in included build`(includedBuild: BuildConfigurator) {
        // given
        includedBuild {
            buildGradleKts {
                """
                plugins {
                    embeddedKotlin("jvm")
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
                    id("dev.panuszewski.typesafe-conventions") version "$projectVersion"
                }
                """
            }
        }

        // when
        val result = runGradle("help")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
    }
}