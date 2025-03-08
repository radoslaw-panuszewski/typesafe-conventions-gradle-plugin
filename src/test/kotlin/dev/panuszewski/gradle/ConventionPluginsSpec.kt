package dev.panuszewski.gradle

import dev.panuszewski.gradle.util.BaseGradleSpec
import dev.panuszewski.gradle.util.BuildOutcome.BUILD_FAILED
import dev.panuszewski.gradle.util.BuildOutcome.BUILD_SUCCESSFUL
import dev.panuszewski.gradle.util.IncludedBuildConfigurator
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ConventionPluginsSpec : BaseGradleSpec() {

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should allow to use catalog accessors in convention plugin`(configurator: IncludedBuildConfigurator) {
        // given
        val someLibrary = "org.apache.commons:commons-lang3:3.17.0"
        accessorUsedInConventionPlugin(someLibrary, configurator)

        // when
        val result = runGradle("dependencyInsight", "--dependency", someLibrary)

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain someLibrary
        result.output shouldNotContain "$someLibrary FAILED"
    }

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should allow to use catalog accessors in plugins block of convention plugin`(configurator: IncludedBuildConfigurator) {
        // given
        val somePlugin = "pl.allegro.tech.build.axion-release"
        val somePluginVersion = "1.18.16"
        val taskRegisteredBySomePlugin = "verifyRelease"

        // and
        accessorUsedInPluginsBlockOfConventionPlugin(somePlugin, somePluginVersion, configurator)

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
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion >= GradleVersion.version("8.8"))

        // given
        val somePlugin = "pl.allegro.tech.build.axion-release"
        val somePluginVersion = "1.18.16"

        // and
        accessorUsedInPluginsBlockOfConventionPlugin(somePlugin, somePluginVersion, includedBuildForConventionPlugins)

        // and
        includedBuildForConventionPlugins {
            appendToSettingsGradleKts {
                """
                typesafeConventions {
                    accessorsInPluginsBlock = false
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
    fun `should respect disabling accessors in plugins block in old Gradle`(
        includedBuildForConventionPlugins: IncludedBuildConfigurator
    ) {
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion < GradleVersion.version("8.8"))

        // given
        val somePlugin = "pl.allegro.tech.build.axion-release"
        val somePluginVersion = "1.18.16"

        // and
        accessorUsedInPluginsBlockOfConventionPlugin(somePlugin, somePluginVersion, includedBuildForConventionPlugins)

        // and
        includedBuildForConventionPlugins {
            prependToSettingsGradleKts {
                """
                import dev.panuszewski.gradle.TypesafeConventionsExtension
                """
            }
            appendToSettingsGradleKts {
                """
                configure<TypesafeConventionsExtension> {
                    accessorsInPluginsBlock = false
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
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion >= GradleVersion.version("8.8"))

        // given
        val somePlugin = "pl.allegro.tech.build.axion-release"
        val somePluginVersion = "1.18.16"

        // and
        accessorUsedInPluginsBlockOfConventionPlugin(somePlugin, somePluginVersion, includedBuildForConventionPlugins)

        // and
        includedBuildForConventionPlugins {
            appendToSettingsGradleKts {
                """
                typesafeConventions {
                    autoPluginDependencies = false
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

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should respect disabling auto plugin dependencies in old Gradle`(
        includedBuildForConventionPlugins: IncludedBuildConfigurator
    ) {
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion < GradleVersion.version("8.8"))

        // given
        val somePlugin = "pl.allegro.tech.build.axion-release"
        val somePluginVersion = "1.18.16"

        // and
        accessorUsedInPluginsBlockOfConventionPlugin(somePlugin, somePluginVersion, includedBuildForConventionPlugins)

        // and
        includedBuildForConventionPlugins {
            prependToSettingsGradleKts {
                """
                import dev.panuszewski.gradle.TypesafeConventionsExtension
                """
            }
            appendToSettingsGradleKts {
                """
                configure<TypesafeConventionsExtension> {
                    autoPluginDependencies = false
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