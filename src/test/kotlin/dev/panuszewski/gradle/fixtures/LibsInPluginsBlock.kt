package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.BuildConfigurator
import dev.panuszewski.gradle.framework.NoConfigFixture

object LibsInPluginsBlock : NoConfigFixture {

    const val pluginId = "pl.allegro.tech.build.axion-release"
    const val pluginMarker = "$pluginId:$pluginId.gradle.plugin"
    const val pluginVersion = "1.18.16"
    const val taskRegisteredByPlugin = "verifyRelease"

    override fun install(spec: GradleSpec, includedBuild: BuildConfigurator, config: Unit) {
        spec.libsVersionsToml {
            """
            [plugins]
            some-plugin = { id = "$pluginId", version = "$pluginVersion" }
            """
        }

        spec.installFixture(ConventionPlugin) {
            pluginBody = """
                plugins {
                    alias(libs.plugins.some.plugin)
                }
                """
        }
    }
}