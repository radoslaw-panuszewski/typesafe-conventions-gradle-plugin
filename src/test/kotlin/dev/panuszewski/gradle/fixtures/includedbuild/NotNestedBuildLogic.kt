package dev.panuszewski.gradle.fixtures.includedbuild

import dev.panuszewski.gradle.framework.GradleBuild
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object NotNestedBuildLogic : NoConfigFixture {

    override fun GradleSpec.install() {
        val name = "build-logic-for-${mainBuild.rootDir.name}"

        settingsGradleKts {
            append {
                """
                includeBuild("../$name")    
                """
            }
        }
        includedBuilds[name] = GradleBuild(
            rootProjectName = name,
            rootDir = mainBuild.rootDir.resolve("../$name"),
            gradleVersion = gradleVersion
        )
    }
}