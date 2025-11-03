package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.fixtures.LibsInPluginsBlockInCustomLocation.Config
import dev.panuszewski.gradle.framework.Fixture
import dev.panuszewski.gradle.framework.GradleSpec

object LibsInPluginsBlockInCustomLocation : Fixture<Config> {

    const val pluginId = "pl.allegro.tech.build.axion-release"
    const val pluginVersion = "1.18.16"
    const val taskRegisteredByPlugin = "verifyRelease"

    override fun GradleSpec.install(config: Config) {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)

        installFixture(ConventionPluginInCustomLocation) {
            pluginName = "some-convention"
            pluginBody = """
                plugins {
                    alias(libs.plugins.some.plugin)
                }
                """
            sourceSet = config.sourceSet
            sourceDirectory = config.sourceDirectory
        }

        libsVersionsToml {
            """
            [plugins]
            some-plugin = { id = "$pluginId", version = "$pluginVersion" }
            """
        }
    }

    override fun defaultConfig() = Config()

    data class Config(
        var sourceSet: String? = null,
        var sourceDirectory: String? = null,
    )
}
