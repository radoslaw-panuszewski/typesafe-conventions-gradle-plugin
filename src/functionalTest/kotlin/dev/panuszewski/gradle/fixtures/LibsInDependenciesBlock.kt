package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object LibsInDependenciesBlock : NoConfigFixture {

    const val someLibrary = "org.apache.commons:commons-lang3:3.17.0"

    override fun GradleSpec.install() {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)

        installFixture(ConventionPluginApplied) {
            pluginBody = """
                plugins {
                    java
                }
                
                dependencies {
                    implementation(libs.some.library)
                }
                """
        }

        libsVersionsToml {
            """
            [libraries]
            some-library = "$someLibrary"
            """
        }
    }
}
