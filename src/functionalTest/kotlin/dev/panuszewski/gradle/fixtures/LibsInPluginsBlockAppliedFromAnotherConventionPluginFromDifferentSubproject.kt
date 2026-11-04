@file:Suppress("ConstPropertyName")

package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object LibsInPluginsBlockAppliedFromAnotherConventionPluginFromDifferentSubproject : NoConfigFixture {

    const val pluginId = "pl.allegro.tech.build.axion-release"
    const val pluginVersion = "1.18.16"
    const val taskRegisteredByPlugin = "verifyRelease"

    override fun GradleSpec.install() {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)

        buildGradleKts {
            """
            plugins {
                id("second-convention")
            }

            repositories {
                mavenCentral()
            }
            """
        }

        includedBuild {
            subprojectBuildGradleKts("first-convention") {
                """
                plugins {
                    `kotlin-dsl`
                }

                repositories {
                    gradlePluginPortal()
                }
                """
            }

            subprojectBuildGradleKts("second-convention") {
                """
                plugins {
                    `kotlin-dsl`
                }

                repositories {
                    gradlePluginPortal()
                }

                dependencies {
                    implementation(project(":first-convention"))
                }
                """
            }

            customProjectFile("first-convention/src/main/kotlin/first-convention.gradle.kts") {
                """
                plugins {
                    alias(libs.plugins.some.plugin)
                }
                """
            }

            customProjectFile("second-convention/src/main/kotlin/second-convention.gradle.kts") {
                """
                plugins {
                    id("first-convention")
                }
                """
            }
        }

        libsVersionsToml {
            """
            [plugins]
            some-plugin = { id = "$pluginId", version = "$pluginVersion" }
            """
        }
    }
}
