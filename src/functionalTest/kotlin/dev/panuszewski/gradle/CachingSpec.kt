package dev.panuszewski.gradle

import dev.panuszewski.gradle.fixtures.IncludedBuildConfiguredForHostingConventions
import dev.panuszewski.gradle.fixtures.LibsInDependenciesBlock
import dev.panuszewski.gradle.fixtures.LibsInPluginsBlock
import dev.panuszewski.gradle.fixtures.TypesafeConventionsAppliedToIncludedBuild
import dev.panuszewski.gradle.fixtures.includedbuild.BuildLogic
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.shouldFail
import dev.panuszewski.gradle.framework.shouldSucceed
import io.kotest.matchers.collections.shouldContain
import org.junit.jupiter.api.Test

class CachingSpec : GradleSpec() {

    @Test
    fun `should generateEntrypointForLibs be UP-TO-DATE`() {
        // given
        installFixture(BuildLogic)
        installFixture(LibsInDependenciesBlock)

        // when
        val firstResult = runGradle(":build-logic:generateEntrypointForLibs")
        val secondResult = runGradle(":build-logic:generateEntrypointForLibs")

        // then
        firstResult.shouldSucceed()
        secondResult.shouldSucceed()
        firstResult.output.lines() shouldContain "> Task :build-logic:generateEntrypointForLibs"
        secondResult.output.lines() shouldContain "> Task :build-logic:generateEntrypointForLibs UP-TO-DATE"
    }

    @Test
    fun `should generateEntrypointForLibs be FROM-CACHE`() {
        // given
        installFixture(BuildLogic)
        installFixture(LibsInDependenciesBlock)

        // when
        val firstResult = runGradle(":build-logic:generateEntrypointForLibs")
        runGradle(":build-logic:clean")
        val secondResult = runGradle(":build-logic:generateEntrypointForLibs")

        // then
        firstResult.shouldSucceed()
        secondResult.shouldSucceed()
        firstResult.output.lines() shouldContain "> Task :build-logic:generateEntrypointForLibs"
        secondResult.output.lines() shouldContain "> Task :build-logic:generateEntrypointForLibs FROM-CACHE"
    }

    @Test
    fun `should generateLibrariesForLibs be UP-TO-DATE`() {
        // given
        installFixture(BuildLogic)
        installFixture(LibsInDependenciesBlock)

        // when
        val firstResult = runGradle(":build-logic:generateLibrariesForLibs")
        val secondResult = runGradle(":build-logic:generateLibrariesForLibs")

        // then
        firstResult.shouldSucceed()
        secondResult.shouldSucceed()
        firstResult.output.lines() shouldContain "> Task :build-logic:generateLibrariesForLibs"
        secondResult.output.lines() shouldContain "> Task :build-logic:generateLibrariesForLibs UP-TO-DATE"
    }

    @Test
    fun `should generateLibrariesForLibs be FROM-CACHE`() {
        // given
        installFixture(BuildLogic)
        installFixture(LibsInDependenciesBlock)

        // when
        val firstResult = runGradle(":build-logic:generateLibrariesForLibs")
        runGradle(":build-logic:clean")
        val secondResult = runGradle(":build-logic:generateLibrariesForLibs")

        // then
        firstResult.shouldSucceed()
        secondResult.shouldSucceed()
        firstResult.output.lines() shouldContain "> Task :build-logic:generateLibrariesForLibs"
        secondResult.output.lines() shouldContain "> Task :build-logic:generateLibrariesForLibs FROM-CACHE"
    }

    @Test
    fun `should invalidate generateLibrariesForLibs when version catalog changes`() {
        // given
        installFixture(BuildLogic)
        val fixture = installFixture(LibsInDependenciesBlock)

        // when
        val firstResult = runGradle(":build-logic:generateLibrariesForLibs")

        libsVersionsToml {
            """
            [libraries]
            some-library = "${fixture.someLibrary}"
            another-library = "${fixture.someLibrary}"
            """
        }
        val secondResult = runGradle(":build-logic:generateLibrariesForLibs")

        // then
        firstResult.shouldSucceed()
        secondResult.shouldSucceed()
        firstResult.output.lines() shouldContain "> Task :build-logic:generateLibrariesForLibs"
        secondResult.output.lines() shouldContain "> Task :build-logic:generateLibrariesForLibs"
    }

    @Test
    fun `should extractPrecompiledScriptPluginPlugins be UP-TO-DATE`() {
        // given
        installFixture(BuildLogic)
        installFixture(LibsInPluginsBlock)

        // when
        val firstResult = runGradle(":build-logic:extractPrecompiledScriptPluginPlugins")
        val secondResult = runGradle(":build-logic:extractPrecompiledScriptPluginPlugins")

        // then
        firstResult.shouldSucceed()
        secondResult.shouldSucceed()
        firstResult.output.lines() shouldContain "> Task :build-logic:extractPrecompiledScriptPluginPlugins"
        secondResult.output.lines() shouldContain "> Task :build-logic:extractPrecompiledScriptPluginPlugins UP-TO-DATE"
    }

    @Test
    fun `should extractPrecompiledScriptPluginPlugins be FROM-CACHE`() {
        // given
        installFixture(BuildLogic)
        installFixture(LibsInPluginsBlock)

        // when
        val firstResult = runGradle(":build-logic:extractPrecompiledScriptPluginPlugins")
        runGradle(":build-logic:clean")
        val secondResult = runGradle(":build-logic:extractPrecompiledScriptPluginPlugins")

        // then
        firstResult.shouldSucceed()
        secondResult.shouldSucceed()
        firstResult.output.lines() shouldContain "> Task :build-logic:extractPrecompiledScriptPluginPlugins"
        secondResult.output.lines() shouldContain "> Task :build-logic:extractPrecompiledScriptPluginPlugins FROM-CACHE"
    }

    @Test
    fun `should invalidate extractPrecompiledScriptPluginPlugins when plugins in version catalog changes`() {
        // given
        installFixture(BuildLogic)
        installFixture(LibsInPluginsBlock)

        // when
        val firstResult = runGradle(":build-logic:extractPrecompiledScriptPluginPlugins")

        // and then plugins in libs.versions.toml are changed
        libsVersionsToml {
            """
            [plugins]
            some-plugin = { id = "com.github.ben-manes.versions", version = "0.52.0" }
            """
        }

        // when
        val secondResult = runGradle(":build-logic:extractPrecompiledScriptPluginPlugins")

        // then
        firstResult.shouldSucceed()
        secondResult.shouldSucceed()
        firstResult.output.lines() shouldContain "> Task :build-logic:extractPrecompiledScriptPluginPlugins"
        secondResult.output.lines() shouldContain "> Task :build-logic:extractPrecompiledScriptPluginPlugins"
    }

    @Test
    fun `should invalidate extractPrecompiledScriptPluginPlugins for multiple catalogs`() {
        // given
        installFixture(BuildLogic)
        installFixture(LibsInPluginsBlock)

        customProjectFile("gradle/custom.versions.toml") {
            """
            [plugins]
            another-plugin = { id = "org.example.custom-plugin", version = "1.0.0" }
            """
        }

        // when
        val firstResult = runGradle(":build-logic:extractPrecompiledScriptPluginPlugins")

        // and then plugins in libs.versions.toml are changed
        libsVersionsToml {
            """
            [plugins]
            some-plugin = { id = "com.github.ben-manes.versions", version = "0.52.0" }
            """
        }

        // when
        val secondResult = runGradle(":build-logic:extractPrecompiledScriptPluginPlugins")

        // then
        firstResult.shouldSucceed()
        secondResult.shouldSucceed()
        firstResult.output.lines() shouldContain "> Task :build-logic:extractPrecompiledScriptPluginPlugins"
        secondResult.output.lines() shouldContain "> Task :build-logic:extractPrecompiledScriptPluginPlugins"
    }

    @Test
    fun `should not invalidate extractPrecompiledScriptPluginPlugins when libraries in version catalog changes`() {
        // given
        installFixture(BuildLogic)
        installFixture(LibsInPluginsBlock)

        // when
        val firstResult = runGradle(":build-logic:extractPrecompiledScriptPluginPlugins")

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
        val secondResult = runGradle(":build-logic:extractPrecompiledScriptPluginPlugins")

        // then
        firstResult.shouldSucceed()
        secondResult.shouldSucceed()
        firstResult.output.lines() shouldContain "> Task :build-logic:extractPrecompiledScriptPluginPlugins"
        secondResult.output.lines() shouldContain "> Task :build-logic:extractPrecompiledScriptPluginPlugins UP-TO-DATE"
    }

    @Test
    fun `should verifyEarlyEvaluatedBuild be UP-TO-DATE`() {
        // given
        installFixture(BuildLogic)
        installFixture(LibsInDependenciesBlock)

        // when
        val firstResult = runGradle(":build-logic:verifyEarlyEvaluatedBuild")
        val secondResult = runGradle(":build-logic:verifyEarlyEvaluatedBuild")

        // then
        firstResult.shouldSucceed()
        secondResult.shouldSucceed()
        firstResult.output.lines() shouldContain "> Task :build-logic:verifyEarlyEvaluatedBuild"
        secondResult.output.lines() shouldContain "> Task :build-logic:verifyEarlyEvaluatedBuild UP-TO-DATE"
    }

    @Test
    fun `should verifyEarlyEvaluatedBuild be FROM-CACHE`() {
        // given
        installFixture(BuildLogic)
        installFixture(LibsInDependenciesBlock)

        // when
        val firstResult = runGradle(":build-logic:verifyEarlyEvaluatedBuild")
        runGradle(":build-logic:clean")
        val secondResult = runGradle(":build-logic:verifyEarlyEvaluatedBuild")

        // then
        firstResult.shouldSucceed()
        secondResult.shouldSucceed()
        firstResult.output.lines() shouldContain "> Task :build-logic:verifyEarlyEvaluatedBuild"
        secondResult.output.lines() shouldContain "> Task :build-logic:verifyEarlyEvaluatedBuild FROM-CACHE"
    }

    @Test
    fun `should invalidate verifyEarlyEvaluatedBuild when it becomes early-evaluated build`() {
        // given
        installFixture(BuildLogic)
        installFixture(LibsInDependenciesBlock)

        // when
        val firstResult = runGradle(":build-logic:verifyEarlyEvaluatedBuild")

        settingsGradleKts {
            """
            pluginManagement {
                includeBuild("build-logic")
            }
            
            plugins {
                id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
            }
            """
        }

        val secondResult = runGradle(":build-logic:verifyEarlyEvaluatedBuild")

        // then
        firstResult.shouldSucceed()
        secondResult.shouldFail()
        firstResult.output.lines() shouldContain "> Task :build-logic:verifyEarlyEvaluatedBuild"
        secondResult.output.lines() shouldContain "> Task :build-logic:verifyEarlyEvaluatedBuild FAILED"
    }

    @Test
    fun `should verifyTopLevelBuild be UP-TO-DATE`() {
        // given
        installFixture(BuildLogic)
        installFixture(LibsInDependenciesBlock)

        // when
        val firstResult = runGradle(":build-logic:verifyTopLevelBuild")
        val secondResult = runGradle(":build-logic:verifyTopLevelBuild")

        // then
        firstResult.shouldSucceed()
        secondResult.shouldSucceed()
        firstResult.output.lines() shouldContain "> Task :build-logic:verifyTopLevelBuild"
        secondResult.output.lines() shouldContain "> Task :build-logic:verifyTopLevelBuild UP-TO-DATE"
    }

    @Test
    fun `should verifyTopLevelBuild be FROM-CACHE`() {
        // given
        installFixture(BuildLogic)
        installFixture(LibsInDependenciesBlock)

        // when
        val firstResult = runGradle(":build-logic:verifyTopLevelBuild")
        runGradle(":build-logic:clean")
        val secondResult = runGradle(":build-logic:verifyTopLevelBuild")

        // then
        firstResult.shouldSucceed()
        secondResult.shouldSucceed()
        firstResult.output.lines() shouldContain "> Task :build-logic:verifyTopLevelBuild"
        secondResult.output.lines() shouldContain "> Task :build-logic:verifyTopLevelBuild FROM-CACHE"
    }

    @Test
    fun `should invalidate verifyTopLevelBuild when it becomes top-level build`() {
        // given
        installFixture(BuildLogic)
        installFixture(TypesafeConventionsAppliedToIncludedBuild)
        installFixture(IncludedBuildConfiguredForHostingConventions)

        // when
        val firstResult = runGradle(":build-logic:verifyTopLevelBuild")

        settingsGradleKts { "" }

        val secondResult = runGradle("verifyTopLevelBuild") {
            withProjectDir(singleIncludedBuild().rootDir)
        }

        // then
        firstResult.shouldSucceed()
        secondResult.shouldFail()
        firstResult.output.lines() shouldContain "> Task :build-logic:verifyTopLevelBuild"
        secondResult.output.lines() shouldContain "> Task :verifyTopLevelBuild FAILED"
    }
}
