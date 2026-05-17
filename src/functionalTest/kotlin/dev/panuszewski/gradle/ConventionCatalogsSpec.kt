package dev.panuszewski.gradle

import dev.panuszewski.gradle.fixtures.ConventionCatalogDisabled
import dev.panuszewski.gradle.fixtures.ConventionCatalogUsedFromConventionPlugin
import dev.panuszewski.gradle.fixtures.ConventionCatalogUsedInParentBuild
import dev.panuszewski.gradle.fixtures.ConventionCatalogUsedInParentBuildThatIsNotRootBuild
import dev.panuszewski.gradle.fixtures.ConventionCatalogUsedInRootBuildThatIsNotDirectParent
import dev.panuszewski.gradle.fixtures.ConventionCatalogWithCustomLocationInSubproject
import dev.panuszewski.gradle.fixtures.ConventionCatalogWithPluginInCustomLocation
import dev.panuszewski.gradle.fixtures.ConventionPluginNamesNotUnique
import dev.panuszewski.gradle.fixtures.CustomConventionCatalogName
import dev.panuszewski.gradle.fixtures.HyphensEncodedInConventionCatalog
import dev.panuszewski.gradle.fixtures.IgnorePackageNames
import dev.panuszewski.gradle.fixtures.IgnorePackageNamesNotUnique
import dev.panuszewski.gradle.fixtures.IgnorePackageNamesWithHyphens
import dev.panuszewski.gradle.fixtures.PackageNameEncodedInConventionCatalog
import dev.panuszewski.gradle.fixtures.TypesafeConventionsAppliedToIncludedBuild
import dev.panuszewski.gradle.fixtures.includedbuild.BuildLogic
import dev.panuszewski.gradle.fixtures.includedbuild.BuildSrc
import dev.panuszewski.gradle.framework.BuildOutcome.BUILD_FAILED
import dev.panuszewski.gradle.framework.BuildOutcome.BUILD_SUCCESSFUL
import dev.panuszewski.gradle.framework.Fixture
import dev.panuszewski.gradle.framework.GradleSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ConventionCatalogsSpec : GradleSpec() {

    @ParameterizedTest
    @IncludedBuildTypesExceptBuildSrc
    fun `should generate typesafe accessor for convention plugin and use it from parent build`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        installFixture(ConventionCatalogUsedInParentBuild)

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain "Hello from someConvention"
    }

    @Test
    fun `should generate typesafe accessor for convention plugin and use it from parent build in multi-level hierarchy`() {
        // given
        installFixture(ConventionCatalogUsedInParentBuildThatIsNotRootBuild)

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain "Hello from someConvention"
    }

    /**
     * It would be problematic in the following hierarchy:
     * - build A includes B1 and B2
     * - build B1 includes build-logic
     * - build B2 includes build-logic
     *
     * (root build would receive convention catalog with conflicting declarations)
     */
    @Test
    fun `should not add convention catalog to root build in multi-level hierarchy`() {
        // given
        installFixture(ConventionCatalogUsedInRootBuildThatIsNotDirectParent)

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_FAILED
        result shouldReportUnresolvedReference "conventions"
    }

    @Test
    fun `should generate typesafe accessor for convention plugin and use it from another convention plugin`() {
        // given
        installFixture(BuildLogic)
        installFixture(ConventionCatalogUsedFromConventionPlugin)

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain "Hello from anotherConvention"
    }

    @ParameterizedTest
    @IncludedBuildTypesExceptBuildSrc
    fun `should encode hyphens in convention name as dots`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        installFixture(HyphensEncodedInConventionCatalog)

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain "Hello from someConvention"
    }

    @ParameterizedTest
    @IncludedBuildTypesExceptBuildSrc
    fun `should encode package name in convention plugin alias`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        installFixture(PackageNameEncodedInConventionCatalog)

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain "Hello from someConvention"
    }

    @ParameterizedTest
    @IncludedBuildTypesExceptBuildSrc
    fun `should allow custom name for convention catalog`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        installFixture(CustomConventionCatalogName)

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain "Hello from someConvention"
    }

    @ParameterizedTest
    @IncludedBuildTypesExceptBuildSrc
    fun `should allow ignoring package names`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        installFixture(IgnorePackageNames)

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain "Hello from someConvention"
    }

    @ParameterizedTest
    @IncludedBuildTypesExceptBuildSrc
    fun `should correctly handle hyphens when ignoring package names`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        installFixture(IgnorePackageNamesWithHyphens)

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain "Hello from someConvention"
    }

    @ParameterizedTest
    @IncludedBuildTypesExceptBuildSrc
    fun `should fail when ignoring package names and convention plugin names are not unique`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        installFixture(IgnorePackageNamesNotUnique)

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_FAILED
        result.output shouldContain "Found duplicated convention plugin names: [someConvention]. " +
            "Either set typesafeConventions.conventionCatalog.ignorePackages = false, " +
            "or make every convention plugin name unique."
    }

    @ParameterizedTest
    @IncludedBuildTypesExceptBuildSrc
    fun `should allow duplicate convention plugin names when ignoring package names is disabled`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        installFixture(ConventionPluginNamesNotUnique)

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain "Hello from first someConvention"
    }

    @ParameterizedTest
    @IncludedBuildTypesExceptBuildSrc
    fun `should allow disabling convention catalog`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        installFixture(ConventionCatalogDisabled)

        // when
        val result = runGradle("--info")

        // then
        result.buildOutcome shouldBe BUILD_FAILED
        result.output shouldContain "Convention catalog is explicitly disabled. " +
            "You can enable it by setting typesafeConventions.conventionCatalog.enabled = true"
    }

    @Test
    fun `should skip convention catalog in buildSrc`() {
        // given
        installFixture(BuildSrc)
        installFixture(ConventionCatalogUsedInParentBuild)

        // when
        val result = runGradle("--info")

        // then
        result.buildOutcome shouldBe BUILD_FAILED
        result.output shouldContain "Convention catalog is not supported in buildSrc. Please migrate to build-logic if you want to use it."
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "src/main/kotlin",
            "src/customSourceSet",
            "src",
        ],
    )
    fun `should discover convention plugins in src directories`(sourceDirectory: String) {
        // given
        installFixture(BuildLogic)
        installFixture(TypesafeConventionsAppliedToIncludedBuild)
        installFixture(ConventionCatalogWithPluginInCustomLocation) {
            this.sourceDirectory = sourceDirectory
        }

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain "Hello from someConvention"
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "src/main/kotlin",
            "src/customSourceSet",
            "src",
        ],
    )
    fun `should discover convention plugins in src directories of subprojects`(sourceDirectory: String) {
        // given
        installFixture(BuildLogic)
        installFixture(TypesafeConventionsAppliedToIncludedBuild)
        installFixture(ConventionCatalogWithCustomLocationInSubproject) {
            this.sourceDirectory = sourceDirectory
        }

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain "Hello from someConvention"
    }
}
