@file:Suppress("ConstPropertyName")

package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object LibsInPluginsBlock : NoConfigFixture {

    const val pluginId = "pl.allegro.tech.build.axion-release"
    const val pluginMarker = "$pluginId:$pluginId.gradle.plugin"
    const val pluginVersion = "1.18.16"
    const val taskRegisteredByPlugin = "verifyRelease"

    override fun GradleSpec.install() {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)

        installFixture(ConventionPlugin) {
            pluginBody = """
                plugins {
                    alias(libs.plugins.some.plugin)
                }
                """
        }

        libsVersionsToml {
            """
            [plugins]
            some-plugin = { id = "$pluginId", version = "$pluginVersion" }
            """
        }
    }
}