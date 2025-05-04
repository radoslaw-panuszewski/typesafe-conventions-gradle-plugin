package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.util.GradleSpec
import dev.panuszewski.gradle.util.BuildConfigurator

interface Fixture {
    fun install(spec: GradleSpec, includedBuild: BuildConfigurator): Fixture
}