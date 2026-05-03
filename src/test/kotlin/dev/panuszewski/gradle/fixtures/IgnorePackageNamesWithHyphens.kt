package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object IgnorePackageNamesWithHyphens : NoConfigFixture {

    override fun GradleSpec.install() {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)
        installFixture(IncludedBuildConfiguredForHostingConventions)
        installFixture(TypesafeConventionsConfig) {
            ignorePackageNames = true
        }

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
            customProjectFile("src/main/kotlin/com/example/some-convention.gradle.kts") {
                """
                package com.example    
                    
                println("Hello from someConvention")    
                """
            }
        }
    }
}
