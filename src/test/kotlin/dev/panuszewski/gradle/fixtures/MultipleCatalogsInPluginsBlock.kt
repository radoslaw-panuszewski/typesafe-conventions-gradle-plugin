package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.conventionPluginAppliedInRootProject
import dev.panuszewski.gradle.util.GradleSpec
import dev.panuszewski.gradle.util.BuildConfigurator

object MultipleCatalogsInPluginsBlock : Fixture {

    const val somePluginId = "pl.allegro.tech.build.axion-release"
    const val somePluginVersion = "1.18.16"
    const val taskRegisteredBySomePlugin = "verifyRelease"

    const val anotherPluginId = "com.github.ben-manes.versions"
    const val anotherPluginVersion = "0.52.0"
    const val taskRegisteredByAnotherPlugin = "dependencyUpdates"

    override fun install(spec: GradleSpec, includedBuild: BuildConfigurator): MultipleCatalogsInPluginsBlock {
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