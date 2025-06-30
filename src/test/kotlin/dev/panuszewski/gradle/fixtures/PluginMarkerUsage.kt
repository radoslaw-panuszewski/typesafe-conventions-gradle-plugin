package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.BuildConfigurator
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object PluginMarkerUsage : NoConfigFixture {

    val somePlugin = "pl.allegro.tech.build.axion-release"
    val somePluginVersion = "1.18.16"
    val taskRegisteredBySomePlugin = "verifyRelease"

    override fun GradleSpec.install(includedBuild: BuildConfigurator) {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)

        libsVersionsToml {
            """
            [plugins]
            some-plugin = { id = "$somePlugin", version = "$somePluginVersion" }
            """
        }

        buildGradleKts {
            """
            plugins {
                id("some-convention")
            }
            
            repositories {
                mavenCentral()
            }
            """
        }

        includedBuild {
            buildGradleKts {
                """
                import dev.panuszewski.gradle.pluginMarker
                    
                plugins {
                    `kotlin-dsl`
                } 
                
                repositories {
                    mavenCentral()
                }
                
                dependencies {
                    implementation(pluginMarker(libs.plugins.some.plugin))
                }
                """
            }

            customProjectFile("src/main/kotlin/some-convention.gradle.kts") {
                """
                plugins {
                    id("$somePlugin")
                }
                """
            }
        }
    }
}