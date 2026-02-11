package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.fixtures.IncludedBuildConfiguredForHostingConventions.Config
import dev.panuszewski.gradle.framework.Fixture
import dev.panuszewski.gradle.framework.GradleBuild
import dev.panuszewski.gradle.framework.GradleSpec

object IncludedBuildConfiguredForHostingConventions : Fixture<Config> {

    override fun GradleSpec.install(config: Config) {
        val build = config.build ?: singleIncludedBuild()

        with(build) {
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

    override fun defaultConfig() = Config()

    class Config {
        var build: GradleBuild? = null
    }
}
