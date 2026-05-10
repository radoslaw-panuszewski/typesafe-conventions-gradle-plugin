package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object HyphensEncodedInConventionCatalog : NoConfigFixture {

    override fun GradleSpec.install() {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)
        installFixture(IncludedBuildConfiguredForHostingConventions)

        buildGradleKts {
            """
            plugins {
                alias(conventions.plugins.some.convention)
            }
            
            repositories {
                mavenCentral()
            }
            """
        }

        includedBuild {
            customProjectFile("src/main/kotlin/some-convention.gradle.kts") {
                """
                println("Hello from someConvention")    
                """
            }
        }
    }
}
