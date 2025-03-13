package dev.panuszewski.gradle

import dev.panuszewski.gradle.TypesafeConventionsPlugin.Companion.MINIMAL_GRADLE_VERSION
import dev.panuszewski.gradle.util.BaseGradleSpec
import dev.panuszewski.gradle.util.BuildOutcome.BUILD_FAILED
import dev.panuszewski.gradle.util.gradleVersion
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

class WrongUsageSpec : BaseGradleSpec() {

    @Test
    fun `should not allow applying to project`() {
        // given
        buildGradleKts {
            """
            plugins {
                id("dev.panuszewski.typesafe-conventions") version "$projectVersion"
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
            """
        }

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_FAILED
        result.output shouldContain "The typesafe-conventions plugin must be applied to settings.gradle.kts, " +
            "but attempted to apply it to build.gradle.kts"
    }

    @Test
    fun `should not allow applying to top-level build`() {
        // given
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

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_FAILED
        result.output shouldContain "The typesafe-conventions plugin must be applied to an included build, " +
            "but attempted to apply it to a top-level build"
    }

    @Test
    fun `should require minimal Gradle version`() {
        // given
        gradleVersion = gradleVersion("8.3")

        buildSrc {
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
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_FAILED
        result.output shouldContain "The typesafe-conventions plugin requires Gradle version " +
            "at least $MINIMAL_GRADLE_VERSION, but currently Gradle 8.3 is used."
    }
}