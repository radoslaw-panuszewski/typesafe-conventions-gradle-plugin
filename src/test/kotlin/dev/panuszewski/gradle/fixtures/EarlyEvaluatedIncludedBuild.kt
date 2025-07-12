package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.fixtures.includedbuild.PluginManagementBuildLogic
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object EarlyEvaluatedIncludedBuild : NoConfigFixture {

    override fun GradleSpec.install() {
        installFixture(PluginManagementBuildLogic)
        installFixture(TypesafeConventionsAppliedToIncludedBuild)

        includedBuild {
            buildGradleKts {
                """
                plugins {
                    `kotlin-dsl`    
                }
                
                repositories {
                    mavenCentral()
                }
                """
            }
        }
    }
}