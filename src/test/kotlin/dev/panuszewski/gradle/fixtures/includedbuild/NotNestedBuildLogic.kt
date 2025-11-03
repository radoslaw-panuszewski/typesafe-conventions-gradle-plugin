package dev.panuszewski.gradle.fixtures.includedbuild

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object NotNestedBuildLogic : NoConfigFixture {

    override fun GradleSpec.install() {
        val name = "build-logic-for-${mainBuild.rootDir.name}"

        includedBuilds[name] = mainBuild.registerIncludedBuild("../build-logic-for-${mainBuild.rootDir.name}")
    }
}
