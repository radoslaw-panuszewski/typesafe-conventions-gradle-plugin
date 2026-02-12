package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object PackageNameEncodedInConventionCatalog : NoConfigFixture {

    override fun GradleSpec.install() {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)
        installFixture(IncludedBuildConfiguredForHostingConventions)

        buildGradleKts {
            """
            plugins {
                alias(conventions.plugins.com.example.someConvention)
            }
            
            repositories {
                mavenCentral()
            }
            """
        }

        includedBuild {
            customProjectFile("src/main/kotlin/com/example/someConvention.gradle.kts") {
                """
                package com.example
                    
                println("Hello from someConvention")    
                """
            }
        }
    }
}
