package dev.panuszewski.gradle

import dev.panuszewski.gradle.TypesafeConventionsPlugin.Companion.MINIMAL_GRADLE_VERSION
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.BuildOutcome.BUILD_FAILED
import dev.panuszewski.gradle.framework.BuildOutcome.BUILD_SUCCESSFUL
import dev.panuszewski.gradle.util.gradleVersion
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test

class WrongUsageSpec : GradleSpec() {

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
        result.output shouldContain """
            The typesafe-conventions plugin is applied to a top-level build, but in most cases it should be applied to an included build or buildSrc. If you know what you're doing, allow top-level build in your settings.gradle.kts:

            typesafeConventions { 
                allowTopLevelBuild = true 
            }

            Read more here: https://github.com/radoslaw-panuszewski/typesafe-conventions-gradle-plugin/blob/main/README.md#top-level-build
        """.trimIndent()
    }

    @Test
    fun `should allow applying to top-level build when explicitly allowed`() {
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion >= GradleVersion.version("8.8"))

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
            
            typesafeConventions { 
                allowTopLevelBuild = true 
            }
            """
        }

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
    }

    @Test
    fun `should allow applying to top-level build when explicitly allowed in old Gradle`() {
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion < GradleVersion.version("8.8"))

        // given
        settingsGradleKts {
            """
            import dev.panuszewski.gradle.TypesafeConventionsExtension
                
            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    mavenLocal()
                }
            }
                
            plugins {
                id("dev.panuszewski.typesafe-conventions") version "$projectVersion"
            }
            
            configure<TypesafeConventionsExtension> { 
                allowTopLevelBuild = true 
            }
            """
        }

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
    }

    @Test
    fun `should require minimal Gradle version`() {
        assumeTrue { System.getenv().containsKey("CI") }

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