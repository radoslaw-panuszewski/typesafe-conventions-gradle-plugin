package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object IncludedBuildConfiguredForHostingConventions : NoConfigFixture {

    override fun GradleSpec.install() {
        includedBuild {
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
