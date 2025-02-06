package dev.panuszewski.gradle

import dev.panuszewski.gradle.util.BaseGradleSpec
import dev.panuszewski.gradle.util.BuildOutcome.BUILD_SUCCESSFUL
import dev.panuszewski.gradle.util.IncludedBuildConfigurator
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class PluginMarkerSpec : BaseGradleSpec() {

    @ParameterizedTest
    @MethodSource("buildLogicOrBuildSrcParameters")
    fun `should provide pluginMarker helper method`(
        buildLogicOrBuildSrc: IncludedBuildConfigurator
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

        buildLogicOrBuildSrc {
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
                    id("dev.panuszewski.typesafe-conventions") version "${System.getenv("PROJECT_VERSION")}"
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
}