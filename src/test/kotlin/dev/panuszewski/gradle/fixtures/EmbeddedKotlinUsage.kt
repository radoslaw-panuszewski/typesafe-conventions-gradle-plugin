package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.BuildConfigurator
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object EmbeddedKotlinUsage : NoConfigFixture {

    override fun GradleSpec.install(includedBuild: BuildConfigurator) {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)

        includedBuild {
            buildGradleKts {
                """
                plugins {
                    embeddedKotlin("jvm")
                } 
                
                repositories {
                    mavenCentral()
                }
                """
            }
        }
    }
}