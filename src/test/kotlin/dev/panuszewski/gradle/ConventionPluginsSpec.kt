package dev.panuszewski.gradle

import dev.panuszewski.gradle.fixtures.CommentedPluginUsage
import dev.panuszewski.gradle.fixtures.CustomBuildDirPath
import dev.panuszewski.gradle.fixtures.ImportedCatalog
import dev.panuszewski.gradle.fixtures.LibsInDependenciesBlock
import dev.panuszewski.gradle.fixtures.LibsInPluginsBlock
import dev.panuszewski.gradle.fixtures.MultiLevelBuildHierarchy
import dev.panuszewski.gradle.fixtures.MultipleCatalogsInDependenciesBlock
import dev.panuszewski.gradle.fixtures.MultipleCatalogsInPluginsBlock
import dev.panuszewski.gradle.fixtures.TopLevelBuild
import dev.panuszewski.gradle.fixtures.TypesafeConventionsConfig
import dev.panuszewski.gradle.fixtures.includedbuild.PluginManagementBuildLogic
import dev.panuszewski.gradle.framework.BuildOutcome.BUILD_FAILED
import dev.panuszewski.gradle.framework.BuildOutcome.BUILD_SUCCESSFUL
import dev.panuszewski.gradle.framework.Fixture
import dev.panuszewski.gradle.framework.GradleSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.params.ParameterizedTest

class ConventionPluginsSpec : GradleSpec() {

    private lateinit var testInfo: TestInfo

    @BeforeEach
    fun setup(testInfo: TestInfo) {
        this.testInfo = testInfo
    }

    @ParameterizedTest
    @SupportedIncludedBuilds
    fun `should allow to use catalog accessors in convention plugin`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        val fixture = installFixture(LibsInDependenciesBlock)

        // when
        val result = runGradle("dependencyInsight", "--dependency", fixture.someLibrary)

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain fixture.someLibrary
        result.output shouldNotContain "${fixture.someLibrary} FAILED"
    }

    @ParameterizedTest
    @SupportedIncludedBuilds
    fun `should allow to use catalog accessors in plugins block of convention plugin`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        val fixture = installFixture(LibsInPluginsBlock)

        // when
        val result = runGradle("tasks")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain fixture.taskRegisteredByPlugin
    }

    @ParameterizedTest
    @SupportedIncludedBuilds
    fun `should respect disabling accessors in plugins block`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        installFixture(LibsInPluginsBlock)
        installFixture(TypesafeConventionsConfig) { accessorsInPluginsBlock = false }

        // when
        val result = runGradle("help")

        // then
        result.buildOutcome shouldBe BUILD_FAILED
        result.output shouldContain "Unresolved reference: libs"
    }

    @ParameterizedTest
    @SupportedIncludedBuilds
    fun `should respect disabling auto plugin dependencies`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        val fixture = installFixture(LibsInPluginsBlock)
        installFixture(TypesafeConventionsConfig) { autoPluginDependencies = false }

        // when
        val result = runGradle("help")

        // then
        result.buildOutcome shouldBe BUILD_FAILED
        result.output shouldContain "Plugin [id: '${fixture.pluginId}'] was not found in any of the following sources"
    }

    @ParameterizedTest
    @SupportedIncludedBuilds
    fun `should allow to override auto plugin dependency`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
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
    @SupportedIncludedBuilds
    fun `should support multiple catalogs`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
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
    @SupportedIncludedBuilds
    fun `should support multiple catalogs in plugins block`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
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
        installFixture(TypesafeConventionsConfig) { allowTopLevelBuild = true }

        // when
        val result = runGradle("assemble")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
    }

    @ParameterizedTest
    @SupportedIncludedBuilds
    fun `should support imported version catalogs`(includedBuild: Fixture<*>) {
        // this feature is not supported for early-evaluated builds
        assumeTrue { includedBuild != PluginManagementBuildLogic }

        // given
        installFixture(includedBuild)
        val fixture = installFixture(ImportedCatalog)

        // when
        val result = runGradle("dependencyInsight", "--dependency", fixture.libraryFromCatalog)

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain fixture.libraryFromCatalog
        result.output shouldNotContain "${fixture.libraryFromCatalog} FAILED"
    }

    @ParameterizedTest
    @SupportedIncludedBuilds
    fun `should ignore commented code when adding auto plugin dependencies`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        val fixture = installFixture(CommentedPluginUsage)

        // when
        val buildName = includedBuilds.keys.first().substringAfterLast("/")
        val commentedPluginResult = runGradle(":$buildName:dependencyInsight", "--dependency", fixture.commentedPluginMarker)
        val uncommentedPluginResult = runGradle(":$buildName:dependencyInsight", "--dependency", fixture.uncommentedPluginMarker)

        // then
        commentedPluginResult.buildOutcome shouldBe BUILD_SUCCESSFUL
        commentedPluginResult.output shouldContain "No dependencies matching given input were found"

        // and
        uncommentedPluginResult.buildOutcome shouldBe BUILD_SUCCESSFUL
        uncommentedPluginResult.output shouldContain fixture.uncommentedPluginMarker
        uncommentedPluginResult.output shouldNotContain "${fixture.uncommentedPluginMarker} FAILED"
    }

    @ParameterizedTest
    @SupportedIncludedBuilds
    fun `should allow to change build directory path`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        installFixture(CustomBuildDirPath)

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
    }

    @Test
    fun `should handle multi-level build hierarchy`() {
        // given
        val fixture = installFixture(MultiLevelBuildHierarchy)

        // when
        val result = runGradle(
            ":secondary-build:dependencyInsight",
            "--dependency", fixture.someLibrary,
            "--configuration", "runtimeClasspath"
        )

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain fixture.someLibrary
        result.output shouldNotContain "${fixture.someLibrary} FAILED"
    }
}