package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.BuildConfigurator
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object CommentedPluginUsage : NoConfigFixture {

    const val commentedPluginId = "pl.allegro.tech.build.axion-release"
    const val commentedPluginMarker = "$commentedPluginId:$commentedPluginId.gradle.plugin"
    const val commentedPluginVersion = "1.18.16"

    const val uncommentedPluginId = "com.github.ben-manes.versions"
    const val uncommentedPluginMarker = "$uncommentedPluginId:$uncommentedPluginId.gradle.plugin"
    const val uncommentedPluginVersion = "0.52.0"

    override fun GradleSpec.install(includedBuild: BuildConfigurator) {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)

        libsVersionsToml {
            """
            [plugins]
            commented-plugin = { id = "$commentedPluginId", version = "$commentedPluginVersion" }
            uncommented-plugin = { id = "$uncommentedPluginId", version = "$uncommentedPluginVersion" }
            """
        }

        installFixture(ConventionPlugin) {
            pluginBody = """
                plugins {
                    // alias(libs.plugins.commented.plugin)
                    /* 
                      alias(libs.plugins.commented.plugin) 
                    */
                    alias(libs.plugins.uncommented.plugin)
                    /**
                     * alias(libs.plugins.commented.plugin)
                     */
                }
                """
        }
    }
}