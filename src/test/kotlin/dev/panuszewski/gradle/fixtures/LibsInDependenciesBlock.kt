package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.conventionPluginAppliedInRootProject

class LibsInDependenciesBlock : TestFixture() {

    val someLibrary = "org.apache.commons:commons-lang3:3.17.0"

    override fun installFixture(): LibsInDependenciesBlock {
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