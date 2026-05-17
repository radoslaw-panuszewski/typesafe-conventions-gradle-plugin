package dev.panuszewski.gradle.fixtures.includedbuild

import dev.panuszewski.gradle.framework.GradleBuild
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object PluginManagementBuildLogic : NoConfigFixture {

    override fun GradleSpec.install() {
        settingsGradleKts {
            append {
                """
                pluginManagement {
                    includeBuild("build-logic")
                }
                
                plugins {
                    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
                }
                """
            }
        }
        includedBuilds["build-logic"] = GradleBuild(
            rootProjectName = "build-logic",
            rootDir = mainBuild.rootDir.resolve("build-logic"),
            gradleVersion = gradleVersion
        )
    }
}
