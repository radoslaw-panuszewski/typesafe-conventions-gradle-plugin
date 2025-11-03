package dev.panuszewski.gradle.fixtures.includedbuild

import dev.panuszewski.gradle.framework.GradleBuild
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object BuildSrc : NoConfigFixture {

    override fun GradleSpec.install() {
        includedBuilds["buildSrc"] = GradleBuild(
            rootProjectName = "buildSrc",
            rootDir = mainBuild.rootDir.resolve("buildSrc"),
            gradleVersion = gradleVersion
        )
    }
}
