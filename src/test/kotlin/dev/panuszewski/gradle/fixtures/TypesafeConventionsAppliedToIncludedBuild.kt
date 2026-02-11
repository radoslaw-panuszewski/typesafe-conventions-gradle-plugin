package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.fixtures.TypesafeConventionsAppliedToIncludedBuild.Config
import dev.panuszewski.gradle.framework.Fixture
import dev.panuszewski.gradle.framework.GradleBuild
import dev.panuszewski.gradle.framework.GradleSpec

object TypesafeConventionsAppliedToIncludedBuild : Fixture<Config> {

    override fun GradleSpec.install(config: Config) {
        val build = config.build ?: singleIncludedBuild()

        with(build) {
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

    override fun defaultConfig() = Config()

    class Config {
        var build: GradleBuild? = null
    }
}
