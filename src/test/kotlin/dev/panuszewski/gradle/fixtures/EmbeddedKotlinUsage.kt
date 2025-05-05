package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.BuildConfigurator
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object EmbeddedKotlinUsage : NoConfigFixture {

    override fun install(spec: GradleSpec, includedBuild: BuildConfigurator, config: Unit) {
        with(spec) {
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
}