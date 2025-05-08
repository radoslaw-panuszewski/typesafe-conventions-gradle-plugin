package dev.panuszewski.gradle

import dev.panuszewski.gradle.fixtures.EmbeddedKotlinUsage
import dev.panuszewski.gradle.fixtures.LibsInIncludedBuild
import dev.panuszewski.gradle.fixtures.PluginMarkerUsage
import dev.panuszewski.gradle.framework.BuildConfigurator
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.BuildOutcome.BUILD_SUCCESSFUL
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.params.ParameterizedTest

class IncludedBuildSpec : GradleSpec() {

    @ParameterizedTest
    @AllIncludedBuildTypes
    fun `should allow to use catalog accessors in included build when running task from subproject`(includedBuild: BuildConfigurator) {
        // given
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
    @AllIncludedBuildTypes
    fun `should allow to use catalog accessors in included build`(includedBuild: BuildConfigurator) {
        // given
        installFixture(LibsInIncludedBuild)

        // when
        val result = runGradle("assemble")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
    }

    @ParameterizedTest
    @AllIncludedBuildTypes
    fun `should provide pluginMarker helper method`(includedBuild: BuildConfigurator) {
        // given
        val fixture = installFixture(PluginMarkerUsage)

        // when
        val result = runGradle("tasks")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain fixture.taskRegisteredBySomePlugin
    }

    @ParameterizedTest
    @AllIncludedBuildTypes
    fun `should the pluginMarker method support rich versions`(includedBuild: BuildConfigurator) {
        // given
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
    @AllIncludedBuildTypes
    fun `should not mess up with kotlin dependency in included build`(includedBuild: BuildConfigurator) {
        // given
        installFixture(EmbeddedKotlinUsage)

        // when
        val result = runGradle("help")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
    }
}