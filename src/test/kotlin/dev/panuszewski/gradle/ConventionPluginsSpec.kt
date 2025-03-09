package dev.panuszewski.gradle

import dev.panuszewski.gradle.util.BaseGradleSpec
import dev.panuszewski.gradle.util.BuildOutcome.BUILD_FAILED
import dev.panuszewski.gradle.util.BuildOutcome.BUILD_SUCCESSFUL
import dev.panuszewski.gradle.util.BuildConfigurator
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
    fun `should allow to use catalog accessors in convention plugin`(includedBuild: BuildConfigurator) {
        // given
        val library = "org.apache.commons:commons-lang3:3.17.0"
        accessorUsedInConventionPlugin(library, includedBuild)

        // when
        val result = runGradle("dependencyInsight", "--dependency", library)

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain library
        result.output shouldNotContain "$library FAILED"
    }

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should allow to use catalog accessors in plugins block of convention plugin`(includedBuild: BuildConfigurator) {
        // given
        val pluginId = "pl.allegro.tech.build.axion-release"
        val pluginVersion = "1.18.16"
        val taskRegisteredByPlugin = "verifyRelease"

        // and
        accessorUsedInPluginsBlockOfConventionPlugin(pluginId, pluginVersion, includedBuild)

        // when
        val result = runGradle("tasks")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain taskRegisteredByPlugin
    }

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should respect disabling accessors in plugins block`(
        includedBuild: BuildConfigurator
    ) {
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion >= GradleVersion.version("8.8"))

        // given
        val pluginId = "pl.allegro.tech.build.axion-release"
        val pluginVersion = "1.18.16"

        // and
        accessorUsedInPluginsBlockOfConventionPlugin(pluginId, pluginVersion, includedBuild)

        // and
        includedBuild {
            settingsGradleKts {
                append {
                    """
                    typesafeConventions {
                        accessorsInPluginsBlock = false
                    }
                    """
                }
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
    fun `should respect disabling accessors in plugins block in old Gradle`(includedBuild: BuildConfigurator) {
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion < GradleVersion.version("8.8"))

        // given
        val pluginId = "pl.allegro.tech.build.axion-release"
        val pluginVersion = "1.18.16"

        // and
        accessorUsedInPluginsBlockOfConventionPlugin(pluginId, pluginVersion, includedBuild)

        // and
        includedBuild {
            settingsGradleKts {
                prepend {
                    """
                    import dev.panuszewski.gradle.TypesafeConventionsExtension
                    """
                }
                append {
                    """
                    configure<TypesafeConventionsExtension> {
                        accessorsInPluginsBlock = false
                    }    
                    """
                }
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
    fun `should respect disabling auto plugin dependencies`(includedBuild: BuildConfigurator) {
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion >= GradleVersion.version("8.8"))

        // given
        val pluginId = "pl.allegro.tech.build.axion-release"
        val pluginVersion = "1.18.16"

        // and
        accessorUsedInPluginsBlockOfConventionPlugin(pluginId, pluginVersion, includedBuild)

        // and
        includedBuild {
            settingsGradleKts {
                append {
                    """
                    typesafeConventions {
                        autoPluginDependencies = false
                    }
                    """
                }
            }
        }

        // when
        val result = runGradle("help")

        // then
        result.buildOutcome shouldBe BUILD_FAILED
        result.output shouldContain "Plugin [id: '$pluginId'] was not found in any of the following sources"
    }

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should respect disabling auto plugin dependencies in old Gradle`(includedBuild: BuildConfigurator) {
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion < GradleVersion.version("8.8"))

        // given
        val pluginId = "pl.allegro.tech.build.axion-release"
        val pluginVersion = "1.18.16"

        // and
        accessorUsedInPluginsBlockOfConventionPlugin(pluginId, pluginVersion, includedBuild)

        // and
        includedBuild {
            settingsGradleKts {
                prepend {
                    """
                    import dev.panuszewski.gradle.TypesafeConventionsExtension
                    """
                }
                append {
                    """
                    configure<TypesafeConventionsExtension> {
                        autoPluginDependencies = false
                    }
                    """
                }
            }
        }

        // when
        val result = runGradle("help")

        // then
        result.buildOutcome shouldBe BUILD_FAILED
        result.output shouldContain "Plugin [id: '$pluginId'] was not found in any of the following sources"
    }

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should allow to override auto plugin dependency`(includedBuild: BuildConfigurator) {
        // given
        val pluginId = "pl.allegro.tech.build.axion-release"
        val pluginMarker = "$pluginId:$pluginId.gradle.plugin"
        val pluginVersion = "1.18.16"
        val overriddenPluginVersion = "1.18.15"

        // and
        accessorUsedInPluginsBlockOfConventionPlugin(pluginId, pluginVersion, includedBuild)

        // and
        includedBuild {
            buildGradleKts {
                prepend {
                    """
                    import dev.panuszewski.gradle.TypesafeConventionsExtension    
                    """
                }
                append {
                    """
                    dependencies {
                        implementation("$pluginMarker:$overriddenPluginVersion")
                    }
                    """
                }
            }
        }

        // when
        val buildName = includedBuilds.keys.first().substringAfterLast("/")
        val result = runGradle(":$buildName:dependencyInsight", "--dependency", pluginMarker)

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain "dependencyInsight${System.lineSeparator()}$pluginMarker:$overriddenPluginVersion"
    }
}