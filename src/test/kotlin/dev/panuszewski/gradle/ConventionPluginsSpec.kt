package dev.panuszewski.gradle

import dev.panuszewski.gradle.fixtures.ImportedCatalog
import dev.panuszewski.gradle.fixtures.LibsInDependenciesBlock
import dev.panuszewski.gradle.fixtures.LibsInPluginsBlock
import dev.panuszewski.gradle.fixtures.MultipleCatalogsInDependenciesBlock
import dev.panuszewski.gradle.fixtures.MultipleCatalogsInPluginsBlock
import dev.panuszewski.gradle.fixtures.TopLevelBuild
import dev.panuszewski.gradle.util.BaseGradleSpec
import dev.panuszewski.gradle.util.BuildOutcome.BUILD_FAILED
import dev.panuszewski.gradle.util.BuildOutcome.BUILD_SUCCESSFUL
import dev.panuszewski.gradle.util.BuildConfigurator
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest

class ConventionPluginsSpec : BaseGradleSpec() {

    @ParameterizedTest
    @AllIncludedBuildTypes
    fun `should allow to use catalog accessors in convention plugin`() {
        // given
        val fixture = installFixture(LibsInDependenciesBlock)

        // when
        val result = runGradle("dependencyInsight", "--dependency", fixture.someLibrary)

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain fixture.someLibrary
        result.output shouldNotContain "${fixture.someLibrary} FAILED"
    }

    @ParameterizedTest
    @AllIncludedBuildTypes
    fun `should allow to use catalog accessors in plugins block of convention plugin`() {
        // given
        val fixture = installFixture(LibsInPluginsBlock)

        // when
        val result = runGradle("tasks")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain fixture.taskRegisteredByPlugin
    }

    @ParameterizedTest
    @AllIncludedBuildTypes
    fun `should respect disabling accessors in plugins block`(includedBuild: BuildConfigurator) {
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion >= GradleVersion.version("8.8"))

        // given
        installFixture(LibsInPluginsBlock)

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
    @AllIncludedBuildTypes
    fun `should respect disabling accessors in plugins block in old Gradle`(includedBuild: BuildConfigurator) {
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion < GradleVersion.version("8.8"))

        // given
        installFixture(LibsInPluginsBlock)

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
    @AllIncludedBuildTypes
    fun `should respect disabling auto plugin dependencies`(includedBuild: BuildConfigurator) {
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion >= GradleVersion.version("8.8"))

        // given
        val fixture = installFixture(LibsInPluginsBlock)

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
        result.output shouldContain "Plugin [id: '${fixture.pluginId}'] was not found in any of the following sources"
    }

    @ParameterizedTest
    @AllIncludedBuildTypes
    fun `should respect disabling auto plugin dependencies in old Gradle`(includedBuild: BuildConfigurator) {
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion < GradleVersion.version("8.8"))

        // given
        val fixture = installFixture(LibsInPluginsBlock)

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
        result.output shouldContain "Plugin [id: '${fixture.pluginId}'] was not found in any of the following sources"
    }

    @ParameterizedTest
    @AllIncludedBuildTypes
    fun `should allow to override auto plugin dependency`(includedBuild: BuildConfigurator) {
        // given
        val fixture = installFixture(LibsInPluginsBlock)

        // and
        val overriddenPluginVersion = "1.18.15"

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
                        implementation("${fixture.pluginMarker}:$overriddenPluginVersion")
                    }
                    """
                }
            }
        }

        // when
        val buildName = includedBuilds.keys.first().substringAfterLast("/")
        val result = runGradle(":$buildName:dependencyInsight", "--dependency", fixture.pluginMarker)

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain "dependencyInsight${System.lineSeparator()}${fixture.pluginMarker}:$overriddenPluginVersion"
    }

    @ParameterizedTest
    @AllIncludedBuildTypes
    fun `should support multiple catalogs`() {
        // given
        val fixture = installFixture(MultipleCatalogsInDependenciesBlock)

        // when
        val someLibraryResult = runGradle("dependencyInsight", "--dependency", fixture.someLibrary)
        val anotherLibraryResult = runGradle("dependencyInsight", "--dependency", fixture.anotherLibrary)

        // then
        someLibraryResult.buildOutcome shouldBe BUILD_SUCCESSFUL
        someLibraryResult.output shouldContain fixture.someLibrary
        someLibraryResult.output shouldNotContain "${fixture.someLibrary} FAILED"

        anotherLibraryResult.buildOutcome shouldBe BUILD_SUCCESSFUL
        anotherLibraryResult.output shouldContain fixture.anotherLibrary
        anotherLibraryResult.output shouldNotContain "${fixture.anotherLibrary} FAILED"
    }

    @ParameterizedTest
    @AllIncludedBuildTypes
    fun `should support multiple catalogs in plugins block`() {
        // given
        val fixture = installFixture(MultipleCatalogsInPluginsBlock)

        // when
        val result = runGradle("tasks")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain fixture.taskRegisteredBySomePlugin
        result.output shouldContain fixture.taskRegisteredByAnotherPlugin
    }

    @Test
    fun `should work for top-level build`() {
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion >= GradleVersion.version("8.8"))

        // given
        installFixture(TopLevelBuild)

        // when
        val result = runGradle("assemble")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
    }

    @ParameterizedTest
    @AllIncludedBuildTypes
    fun `should support imported version catalogs`() {
        // given
        val fixture = installFixture(ImportedCatalog)

        // when
        val result = runGradle("dependencyInsight", "--dependency", fixture.libraryFromCatalog)

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain fixture.libraryFromCatalog
        result.output shouldNotContain "${fixture.libraryFromCatalog} FAILED"
    }
}