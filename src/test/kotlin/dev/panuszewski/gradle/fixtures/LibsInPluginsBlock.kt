package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.conventionPluginAppliedInRootProject
import dev.panuszewski.gradle.util.GradleSpec
import dev.panuszewski.gradle.util.BuildConfigurator

object LibsInPluginsBlock : Fixture {

    const val pluginId = "pl.allegro.tech.build.axion-release"
    const val pluginMarker = "$pluginId:$pluginId.gradle.plugin"
    const val pluginVersion = "1.18.16"
    const val taskRegisteredByPlugin = "verifyRelease"

    override fun install(spec: GradleSpec, includedBuild: BuildConfigurator) {
        spec.libsVersionsToml {
            """
            [plugins]
            some-plugin = { id = "$pluginId", version = "$pluginVersion" }
            """
        }

        spec.conventionPluginAppliedInRootProject(includedBuild) {
            """
            plugins {
                alias(libs.plugins.some.plugin)
            }
            """
        }
    }
}