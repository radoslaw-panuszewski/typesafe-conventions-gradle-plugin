package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object MultipleCatalogsInPluginsBlock : NoConfigFixture {

    const val somePluginId = "pl.allegro.tech.build.axion-release"
    const val somePluginVersion = "1.18.16"
    const val taskRegisteredBySomePlugin = "verifyRelease"

    const val anotherPluginId = "com.github.ben-manes.versions"
    const val anotherPluginVersion = "0.52.0"
    const val taskRegisteredByAnotherPlugin = "dependencyUpdates"

    override fun GradleSpec.install() {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)

        installFixture(ConventionPluginApplied) {
            pluginBody = """
                plugins {
                    alias(libs.plugins.some.plugin)
                    alias(tools.plugins.another.plugin)
                }
                """
        }

        libsVersionsToml {
            """
            [plugins]
            some-plugin = { id = "$somePluginId", version = "$somePluginVersion" }
            """
        }

        customProjectFile("gradle/tools.versions.toml") {
            """
            [plugins]
            another-plugin = { id = "$anotherPluginId", version = "$anotherPluginVersion" }
            """
        }

        settingsGradleKts {
            append {
                """
                dependencyResolutionManagement {
                    versionCatalogs {
                        create("tools") {
                            from(files("gradle/tools.versions.toml"))
                        }
                    }
                }
                """
            }
        }
    }
}
