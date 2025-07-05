package dev.panuszewski.gradle.fixtures.includedbuild

import dev.panuszewski.gradle.framework.GradleBuild
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object BuildLogic : NoConfigFixture {

    override fun GradleSpec.install() {
        settingsGradleKts {
            append {
                """
                includeBuild("build-logic")    
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