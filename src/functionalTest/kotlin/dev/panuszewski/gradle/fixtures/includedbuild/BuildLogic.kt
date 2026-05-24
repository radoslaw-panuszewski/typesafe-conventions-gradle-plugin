package dev.panuszewski.gradle.fixtures.includedbuild

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object BuildLogic : NoConfigFixture {

    override fun GradleSpec.install() {
        includedBuilds["build-logic"] = mainBuild.registerIncludedBuild("build-logic")
    }
}
