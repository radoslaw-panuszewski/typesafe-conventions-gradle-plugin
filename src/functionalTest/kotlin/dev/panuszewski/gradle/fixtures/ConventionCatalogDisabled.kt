package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object ConventionCatalogDisabled : NoConfigFixture {

    override fun GradleSpec.install() {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)
        installFixture(IncludedBuildConfiguredForHostingConventions)
        installFixture(TypesafeConventionsConfig) {
            conventionCatalogEnabled = false
        }

        buildGradleKts {
            """
            plugins {
                alias(conventions.plugins.someConvention)
            }
            
            repositories {
                mavenCentral()
            }
            """
        }

        includedBuild {
            customProjectFile("src/main/kotlin/someConvention.gradle.kts") {
                """
                println("Hello from someConvention")    
                """
            }
        }
    }
}
