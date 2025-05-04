package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.conventionPluginAppliedInRootProject
import dev.panuszewski.gradle.util.BaseGradleSpec
import dev.panuszewski.gradle.util.BuildConfigurator

object LibsInDependenciesBlock : Fixture {

    val someLibrary = "org.apache.commons:commons-lang3:3.17.0"

    override fun install(spec: BaseGradleSpec, includedBuild: BuildConfigurator): LibsInDependenciesBlock {
        spec.libsVersionsToml {
            """
            [libraries]
            some-library = "$someLibrary"
            """
        }

        spec.conventionPluginAppliedInRootProject(includedBuild) {
            """
            plugins {
                java
            }
            
            dependencies {
                implementation(libs.some.library)
            }
            """
        }
        return this
    }
}