package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.conventionPluginAppliedInRootProject

class LibsInPluginsBlock : TestFixture() {

    val pluginId = "pl.allegro.tech.build.axion-release"
    val pluginMarker = "$pluginId:$pluginId.gradle.plugin"
    val pluginVersion = "1.18.16"
    val taskRegisteredByPlugin = "verifyRelease"

    override fun installFixture(): LibsInPluginsBlock {
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
        return this
    }
}