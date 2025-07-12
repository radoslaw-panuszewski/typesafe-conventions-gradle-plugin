package dev.panuszewski.gradle

import dev.panuszewski.gradle.fixtures.EmbeddedKotlinUsage
import dev.panuszewski.gradle.fixtures.LibsInIncludedBuild
import dev.panuszewski.gradle.fixtures.PluginMarkerUsage
import dev.panuszewski.gradle.fixtures.includedbuild.PluginManagementBuildLogic
import dev.panuszewski.gradle.framework.BuildOutcome.BUILD_SUCCESSFUL
import dev.panuszewski.gradle.framework.Fixture
import dev.panuszewski.gradle.framework.GradleSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest

class IncludedBuildSpec : GradleSpec() {

    @ParameterizedTest
    @SupportedIncludedBuilds
    fun `should allow to use catalog accessors in included build when running task from subproject`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        installFixture(LibsInIncludedBuild)

        // and
        subprojectBuildGradleKts("subproject") {
            """
            plugins {
                java
            }
            """
        }

        // when
        val result = runGradle(":subproject:assemble")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
    }

    @ParameterizedTest
    @SupportedIncludedBuilds
    fun `should allow to use catalog accessors in included build`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        installFixture(LibsInIncludedBuild)

        // when
        val result = runGradle("assemble")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
    }

    @ParameterizedTest
    @SupportedIncludedBuilds
    fun `should provide pluginMarker helper method`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        val fixture = installFixture(PluginMarkerUsage)

        // when
        val result = runGradle("tasks")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain fixture.taskRegisteredBySomePlugin
    }

    @ParameterizedTest
    @SupportedIncludedBuilds
    fun `should the pluginMarker method support rich versions`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        val fixture = installFixture(PluginMarkerUsage)

        // and
        libsVersionsToml {
            """
            [plugins]
            some-plugin = { id = "${fixture.somePlugin}", version.prefer = "${fixture.somePluginVersion}" }
            """
        }

        // when
        val result = runGradle("tasks")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain fixture.taskRegisteredBySomePlugin
    }

    @ParameterizedTest
    @SupportedIncludedBuilds
    fun `should not mess up with kotlin dependency in included build`(includedBuild: Fixture<*>) {
        // given
        installFixture(includedBuild)
        installFixture(EmbeddedKotlinUsage)

        // when
        val result = runGradle("help")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
    }
}