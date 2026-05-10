package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object ConventionCatalogUsedFromConventionPlugin : NoConfigFixture {

    override fun GradleSpec.install() {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)
        installFixture(IncludedBuildConfiguredForHostingConventions)

        buildGradleKts {
            """
            plugins {
                alias(conventions.plugins.someConvention)
            }
            """
        }

        includedBuild {
            customProjectFile("src/main/kotlin/someConvention.gradle.kts") {
                """
                plugins {
                    alias(conventions.plugins.anotherConvention)
                }    
                """
            }

            customProjectFile("src/main/kotlin/anotherConvention.gradle.kts") {
                """
                println("Hello from anotherConvention")
                """
            }
        }
    }
}
