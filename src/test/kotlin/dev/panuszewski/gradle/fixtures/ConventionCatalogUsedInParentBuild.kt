package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object ConventionCatalogUsedInParentBuild : NoConfigFixture {

    override fun GradleSpec.install() {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)
        installFixture(IncludedBuildConfiguredForHostingConventions)

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
            customProjectFile("src/main/kotlin/conventions/someConvention.gradle.kts") {
                """
                package conventions
                    
                println("Hello from someConvention")    
                """
            }
        }
    }
}
