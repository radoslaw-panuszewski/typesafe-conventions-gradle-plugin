package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.BuildConfigurator
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object CustomBuildDirPath : NoConfigFixture {

    override fun install(spec: GradleSpec, includedBuild: BuildConfigurator) {
        with(spec) {
            installFixture(LibsInDependenciesBlock)

            includedBuild {
                buildGradleKts {
                    append {
                        """
                        project.layout.buildDirectory = File(rootProject.projectDir, ".gradle-build/${'$'}{project.name}")
                        """
                    }
                }
            }
        }
    }
}