package dev.panuszewski.gradle

import dev.panuszewski.gradle.util.BaseGradleSpec
import io.kotest.matchers.collections.shouldContain
import org.junit.jupiter.api.Test

class CachingSpec : BaseGradleSpec() {

    @Test
    fun `should generateEntrypointForLibs be UP-TO-DATE`() {
        // given
        val library = "org.apache.commons:commons-lang3:3.17.0"
        libsInDependenciesBlock(library, BaseGradleSpec::buildSrc)

        // when
        val firstResult = runGradle(":buildSrc:generateEntrypointForLibs")
        val secondResult = runGradle(":buildSrc:generateEntrypointForLibs")

        // then
        firstResult.output.lines() shouldContain "> Task :buildSrc:generateEntrypointForLibs"
        secondResult.output.lines() shouldContain "> Task :buildSrc:generateEntrypointForLibs UP-TO-DATE"
    }

    @Test
    fun `should generateEntrypointForLibs be FROM-CACHE`() {
        // given
        val library = "org.apache.commons:commons-lang3:3.17.0"
        libsInDependenciesBlock(library, BaseGradleSpec::buildSrc)

        // when
        val firstResult = runGradle(":buildSrc:generateEntrypointForLibs")
        runGradle(":buildSrc:clean")
        val secondResult = runGradle(":buildSrc:generateEntrypointForLibs")

        // then
        firstResult.output.lines() shouldContain "> Task :buildSrc:generateEntrypointForLibs"
        secondResult.output.lines() shouldContain "> Task :buildSrc:generateEntrypointForLibs FROM-CACHE"
    }

    @Test
    fun `should generateLibrariesForLibs be UP-TO-DATE`() {
        // given
        val library = "org.apache.commons:commons-lang3:3.17.0"
        libsInDependenciesBlock(library, BaseGradleSpec::buildSrc)

        // when
        val firstResult = runGradle(":buildSrc:generateLibrariesForLibs")
        val secondResult = runGradle(":buildSrc:generateLibrariesForLibs")

        // then
        firstResult.output.lines() shouldContain "> Task :buildSrc:generateLibrariesForLibs"
        secondResult.output.lines() shouldContain "> Task :buildSrc:generateLibrariesForLibs UP-TO-DATE"
    }

    @Test
    fun `should generateLibrariesForLibs be FROM-CACHE`() {
        // given
        val library = "org.apache.commons:commons-lang3:3.17.0"
        libsInDependenciesBlock(library, BaseGradleSpec::buildSrc)

        // when
        val firstResult = runGradle(":buildSrc:generateLibrariesForLibs")
        runGradle(":buildSrc:clean")
        val secondResult = runGradle(":buildSrc:generateLibrariesForLibs")

        // then
        firstResult.output.lines() shouldContain "> Task :buildSrc:generateLibrariesForLibs"
        secondResult.output.lines() shouldContain "> Task :buildSrc:generateLibrariesForLibs FROM-CACHE"
    }

    @Test
    fun `should invalidate generateLibrariesForLibs when version catalog changes`() {
        // given
        val library = "org.apache.commons:commons-lang3:3.17.0"
        libsInDependenciesBlock(library, BaseGradleSpec::buildSrc)

        // when
        val firstResult = runGradle(":buildSrc:generateLibrariesForLibs")

        customProjectFile("gradle/libs.versions.toml") {
            """
            [libraries]
            some-library = "$library"
            another-library = "$library"
            """
        }
        val secondResult = runGradle(":buildSrc:generateLibrariesForLibs")

        // then
        firstResult.output.lines() shouldContain "> Task :buildSrc:generateLibrariesForLibs"
        secondResult.output.lines() shouldContain "> Task :buildSrc:generateLibrariesForLibs"
    }

    @Test
    fun `should extractPrecompiledScriptPluginPlugins be UP-TO-DATE`() {
        // given
        val pluginId = "pl.allegro.tech.build.axion-release"
        val pluginVersion = "1.18.16"
        libsInPluginsBlock(pluginId, pluginVersion, BaseGradleSpec::buildSrc)

        // when
        val firstResult = runGradle(":buildSrc:extractPrecompiledScriptPluginPlugins")
        val secondResult = runGradle(":buildSrc:extractPrecompiledScriptPluginPlugins")

        // then
        firstResult.output.lines() shouldContain "> Task :buildSrc:extractPrecompiledScriptPluginPlugins"
        secondResult.output.lines() shouldContain "> Task :buildSrc:extractPrecompiledScriptPluginPlugins UP-TO-DATE"
    }

    @Test
    fun `should extractPrecompiledScriptPluginPlugins be FROM-CACHE`() {
        // given
        val pluginId = "pl.allegro.tech.build.axion-release"
        val pluginVersion = "1.18.16"
        libsInPluginsBlock(pluginId, pluginVersion, BaseGradleSpec::buildSrc)

        // when
        val firstResult = runGradle(":buildSrc:extractPrecompiledScriptPluginPlugins")
        runGradle(":buildSrc:clean")
        val secondResult = runGradle(":buildSrc:extractPrecompiledScriptPluginPlugins")

        // then
        firstResult.output.lines() shouldContain "> Task :buildSrc:extractPrecompiledScriptPluginPlugins"
        secondResult.output.lines() shouldContain "> Task :buildSrc:extractPrecompiledScriptPluginPlugins FROM-CACHE"
    }

    @Test
    fun `should invalidate extractPrecompiledScriptPluginPlugins when plugins in version catalog changes`() {
        // given
        val pluginId = "pl.allegro.tech.build.axion-release"
        val pluginVersion = "1.18.16"
        val changedPluginId = "com.github.ben-manes.versions"
        val changedPluginVersion = "0.52.0"
        libsInPluginsBlock(pluginId, pluginVersion, BaseGradleSpec::buildSrc)

        // when
        val firstResult = runGradle(":buildSrc:extractPrecompiledScriptPluginPlugins")

        customProjectFile("gradle/libs.versions.toml") {
            """
            [plugins]
            some-plugin = { id = "$changedPluginId", version = "$changedPluginVersion" }
            """
        }
        val secondResult = runGradle(":buildSrc:extractPrecompiledScriptPluginPlugins")

        // then
        firstResult.output.lines() shouldContain "> Task :buildSrc:extractPrecompiledScriptPluginPlugins"
        secondResult.output.lines() shouldContain "> Task :buildSrc:extractPrecompiledScriptPluginPlugins"
    }

    @Test
    fun `should not invalidate extractPrecompiledScriptPluginPlugins when libraries in version catalog changes`() {
        // given
        val pluginId = "pl.allegro.tech.build.axion-release"
        val pluginVersion = "1.18.16"
        libsInPluginsBlock(pluginId, pluginVersion, BaseGradleSpec::buildSrc)

        // when
        val firstResult = runGradle(":buildSrc:extractPrecompiledScriptPluginPlugins")

        customProjectFile("gradle/libs.versions.toml") {
            """
            [plugins]
            some-plugin = { id = "$pluginId", version = "$pluginVersion" }
            
            [libraries]
            some-library = "org.apache.commons:commons-lang3:3.17.0"
            """
        }
        val secondResult = runGradle(":buildSrc:extractPrecompiledScriptPluginPlugins")

        // then
        firstResult.output.lines() shouldContain "> Task :buildSrc:extractPrecompiledScriptPluginPlugins"
        secondResult.output.lines() shouldContain "> Task :buildSrc:extractPrecompiledScriptPluginPlugins UP-TO-DATE"
    }
}