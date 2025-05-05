package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.BuildConfigurator
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object TypesafeConventionsAppliedToIncludedBuild : NoConfigFixture {

    override fun install(spec: GradleSpec, includedBuild: BuildConfigurator, config: Unit) {
        with(spec) {
            includedBuild {
                settingsGradleKts {
                    """
                    pluginManagement {
                        repositories {
                            gradlePluginPortal()
                            mavenLocal()
                        }
                    }
                        
                    plugins {
                        id("dev.panuszewski.typesafe-conventions") version "$projectVersion"
                    }
                    """
                }
            }
        }
    }
}