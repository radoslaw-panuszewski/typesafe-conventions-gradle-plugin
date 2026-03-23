package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object MultipleConventionPlugins : NoConfigFixture {
    const val somePluginId = "pl.allegro.tech.build.axion-release"
    const val somePluginVersion = "1.18.16"

    const val anotherPluginId = "com.github.ben-manes.versions"
    const val anotherPluginVersion = "0.52.0"

    override fun GradleSpec.install() {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)

        libsVersionsToml {
            """
            [plugins]
            some-plugin = { id = "$somePluginId", version = "$somePluginVersion" }
            another-plugin = { id = "$anotherPluginId", version = "$anotherPluginVersion" }
            """
        }

        includedBuild {
            customProjectFile("src/main/kotlin/some-plugin.gradle.kts") {
                """
                plugins {
                    alias(libs.plugins.some.plugin)
                    alias(libs.plugins.another.plugin)
                }
                """
            }

            customProjectFile("src/main/kotlin/another-plugin.gradle.kts") {
                """
                plugins {
                    alias(libs.plugins.another.plugin)
                    alias(libs.plugins.some.plugin)
                }
                """
            }

            buildGradleKts {
                """
                plugins {
                    `kotlin-dsl`
                }

                repositories {
                    gradlePluginPortal()
                }
                """
            }
        }
    }
}
