package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object CustomBuildDirPath : NoConfigFixture {

    override fun GradleSpec.install() {
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
