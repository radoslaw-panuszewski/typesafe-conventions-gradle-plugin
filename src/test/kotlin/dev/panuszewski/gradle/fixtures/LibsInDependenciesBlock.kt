package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.BuildConfigurator
import dev.panuszewski.gradle.framework.NoConfigFixture

object LibsInDependenciesBlock : NoConfigFixture {

    const val someLibrary = "org.apache.commons:commons-lang3:3.17.0"

    override fun install(spec: GradleSpec, includedBuild: BuildConfigurator, config: Unit) {
        with(spec) {
            libsVersionsToml {
                """
                [libraries]
                some-library = "$someLibrary"
                """
            }

            installFixture(ConventionPlugin) {
                pluginBody = """
                    plugins {
                        java
                    }
                    
                    dependencies {
                        implementation(libs.some.library)
                    }
                    """
            }
        }
    }
}