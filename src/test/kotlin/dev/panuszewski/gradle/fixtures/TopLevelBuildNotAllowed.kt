package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.BuildConfigurator
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object TopLevelBuildNotAllowed : NoConfigFixture {

    override fun install(spec: GradleSpec, includedBuild: BuildConfigurator) {
        spec.installFixture(TopLevelBuild)

        spec.settingsGradleKts {
            append {
                """
                typesafeConventions { 
                    allowTopLevelBuild = false 
                }
                """
            }
        }
    }
}