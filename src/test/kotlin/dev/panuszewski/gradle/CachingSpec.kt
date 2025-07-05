package dev.panuszewski.gradle

import dev.panuszewski.gradle.fixtures.LibsInDependenciesBlock
import dev.panuszewski.gradle.fixtures.LibsInPluginsBlock
import dev.panuszewski.gradle.fixtures.includedbuild.BuildSrc
import dev.panuszewski.gradle.framework.BuildOutcome
import dev.panuszewski.gradle.framework.BuildOutcome.BUILD_SUCCESSFUL
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.shouldAllBuildsSucceed
import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CachingSpec : GradleSpec() {

    @Test
    fun `should generateEntrypointForLibs be UP-TO-DATE`() {
        // given
        installFixture(BuildSrc)
        installFixture(LibsInDependenciesBlock)

        // when
        val firstResult = runGradle(":buildSrc:generateEntrypointForLibs")
        val secondResult = runGradle(":buildSrc:generateEntrypointForLibs")

        // then
        shouldAllBuildsSucceed(firstResult, secondResult)
        firstResult.output.lines() shouldContain "> Task :buildSrc:generateEntrypointForLibs"
        secondResult.output.lines() shouldContain "> Task :buildSrc:generateEntrypointForLibs UP-TO-DATE"
    }

    @Test
    fun `should generateEntrypointForLibs be FROM-CACHE`() {
        // given
        installFixture(BuildSrc)
        installFixture(LibsInDependenciesBlock)

        // when
        val firstResult = runGradle(":buildSrc:generateEntrypointForLibs")
        runGradle(":buildSrc:clean")
        val secondResult = runGradle(":buildSrc:generateEntrypointForLibs")

        // then
        shouldAllBuildsSucceed(firstResult, secondResult)
        firstResult.output.lines() shouldContain "> Task :buildSrc:generateEntrypointForLibs"
        secondResult.output.lines() shouldContain "> Task :buildSrc:generateEntrypointForLibs FROM-CACHE"
    }

    @Test
    fun `should generateLibrariesForLibs be UP-TO-DATE`() {
        // given
        installFixture(BuildSrc)
        installFixture(LibsInDependenciesBlock)

        // when
        val firstResult = runGradle(":buildSrc:generateLibrariesForLibs")
        val secondResult = runGradle(":buildSrc:generateLibrariesForLibs")

        // then
        shouldAllBuildsSucceed(firstResult, secondResult)
        firstResult.output.lines() shouldContain "> Task :buildSrc:generateLibrariesForLibs"
        secondResult.output.lines() shouldContain "> Task :buildSrc:generateLibrariesForLibs UP-TO-DATE"
    }

    @Test
    fun `should generateLibrariesForLibs be FROM-CACHE`() {
        // given
        installFixture(BuildSrc)
        installFixture(LibsInDependenciesBlock)

        // when
        val firstResult = runGradle(":buildSrc:generateLibrariesForLibs")
        runGradle(":buildSrc:clean")
        val secondResult = runGradle(":buildSrc:generateLibrariesForLibs")

        // then
        shouldAllBuildsSucceed(firstResult, secondResult)
        firstResult.output.lines() shouldContain "> Task :buildSrc:generateLibrariesForLibs"
        secondResult.output.lines() shouldContain "> Task :buildSrc:generateLibrariesForLibs FROM-CACHE"
    }

    @Test
    fun `should invalidate generateLibrariesForLibs when version catalog changes`() {
        // given
        installFixture(BuildSrc)
        val fixture = installFixture(LibsInDependenciesBlock)

        // when
        val firstResult = runGradle(":buildSrc:generateLibrariesForLibs")

        libsVersionsToml {
            """
            [libraries]
            some-library = "${fixture.someLibrary}"
            another-library = "${fixture.someLibrary}"
            """
        }
        val secondResult = runGradle(":buildSrc:generateLibrariesForLibs")

        // then
        shouldAllBuildsSucceed(firstResult, secondResult)
        firstResult.output.lines() shouldContain "> Task :buildSrc:generateLibrariesForLibs"
        secondResult.output.lines() shouldContain "> Task :buildSrc:generateLibrariesForLibs"
    }

    @Test
    fun `should extractPrecompiledScriptPluginPlugins be UP-TO-DATE`() {
        // given
        installFixture(BuildSrc)
        installFixture(LibsInPluginsBlock)

        // when
        val firstResult = runGradle(":buildSrc:extractPrecompiledScriptPluginPlugins")
        val secondResult = runGradle(":buildSrc:extractPrecompiledScriptPluginPlugins")

        // then
        shouldAllBuildsSucceed(firstResult, secondResult)
        firstResult.output.lines() shouldContain "> Task :buildSrc:extractPrecompiledScriptPluginPlugins"
        secondResult.output.lines() shouldContain "> Task :buildSrc:extractPrecompiledScriptPluginPlugins UP-TO-DATE"
    }

    @Test
    fun `should extractPrecompiledScriptPluginPlugins be FROM-CACHE`() {
        // given
        installFixture(BuildSrc)
        installFixture(LibsInPluginsBlock)

        // when
        val firstResult = runGradle(":buildSrc:extractPrecompiledScriptPluginPlugins")
        runGradle(":buildSrc:clean")
        val secondResult = runGradle(":buildSrc:extractPrecompiledScriptPluginPlugins")

        // then
        shouldAllBuildsSucceed(firstResult, secondResult)
        firstResult.output.lines() shouldContain "> Task :buildSrc:extractPrecompiledScriptPluginPlugins"
        secondResult.output.lines() shouldContain "> Task :buildSrc:extractPrecompiledScriptPluginPlugins FROM-CACHE"
    }

    @Test
    fun `should invalidate extractPrecompiledScriptPluginPlugins when plugins in version catalog changes`() {
        // given
        installFixture(BuildSrc)
        installFixture(LibsInPluginsBlock)

        // when
        val firstResult = runGradle(":buildSrc:extractPrecompiledScriptPluginPlugins")

        // and then plugins in libs.versions.toml are changed
        libsVersionsToml {
            """
            [plugins]
            some-plugin = { id = "com.github.ben-manes.versions", version = "0.52.0" }
            """
        }

        // when
        val secondResult = runGradle(":buildSrc:extractPrecompiledScriptPluginPlugins")

        // then
        shouldAllBuildsSucceed(firstResult, secondResult)
        firstResult.output.lines() shouldContain "> Task :buildSrc:extractPrecompiledScriptPluginPlugins"
        secondResult.output.lines() shouldContain "> Task :buildSrc:extractPrecompiledScriptPluginPlugins"
    }

    @Test
    fun `should not invalidate extractPrecompiledScriptPluginPlugins when libraries in version catalog changes`() {
        // given
        installFixture(BuildSrc)
        installFixture(LibsInPluginsBlock)

        // when
        val firstResult = runGradle(":buildSrc:extractPrecompiledScriptPluginPlugins")

        // and then libraries in libs.versions.toml are changed (while keeping plugins unchanged)
        libsVersionsToml {
            append {
                """
                [libraries]
                some-library = "org.apache.commons:commons-lang3:3.17.0"
                """
            }
        }

        // when
        val secondResult = runGradle(":buildSrc:extractPrecompiledScriptPluginPlugins")

        // then
        shouldAllBuildsSucceed(firstResult, secondResult)
        firstResult.output.lines() shouldContain "> Task :buildSrc:extractPrecompiledScriptPluginPlugins"
        secondResult.output.lines() shouldContain "> Task :buildSrc:extractPrecompiledScriptPluginPlugins UP-TO-DATE"
    }
}