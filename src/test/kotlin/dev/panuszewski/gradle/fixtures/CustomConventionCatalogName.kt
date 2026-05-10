package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object CustomConventionCatalogName : NoConfigFixture {

    override fun GradleSpec.install() {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)
        installFixture(IncludedBuildConfiguredForHostingConventions)
        installFixture(TypesafeConventionsConfig) {
            conventionCatalogName = "myConventions"
        }

        buildGradleKts {
            """
            plugins {
                alias(myConventions.plugins.someConvention)
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
