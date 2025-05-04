package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.util.BaseGradleSpec
import dev.panuszewski.gradle.util.BuildConfigurator

interface Fixture {
    fun install(spec: BaseGradleSpec, includedBuild: BuildConfigurator): Fixture
}