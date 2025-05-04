package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.conventionPluginAppliedInRootProject

class MultipleCatalogsInPluginsBlock : TestFixture() {

    val somePluginId = "pl.allegro.tech.build.axion-release"
    val somePluginVersion = "1.18.16"
    val taskRegisteredBySomePlugin = "verifyRelease"

    val anotherPluginId = "com.github.ben-manes.versions"
    val anotherPluginVersion = "0.52.0"
    val taskRegisteredByAnotherPlugin = "dependencyUpdates"

    override fun installFixture(): MultipleCatalogsInPluginsBlock {
        spec.libsVersionsToml {
            """
            [plugins]
            some-plugin = { id = "$somePluginId", version = "$somePluginVersion" }
            """
        }

        spec.customProjectFile("gradle/tools.versions.toml") {
            """
            [plugins]
            another-plugin = { id = "$anotherPluginId", version = "$anotherPluginVersion" }
            """
        }

        spec.settingsGradleKts {
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

        spec.conventionPluginAppliedInRootProject(includedBuild) {
            """
            plugins {
                alias(libs.plugins.some.plugin)
                alias(tools.plugins.another.plugin)
            }
            """
        }

        return this
    }
}