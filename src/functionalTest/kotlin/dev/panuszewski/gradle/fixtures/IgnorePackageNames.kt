package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object IgnorePackageNames : NoConfigFixture {

    override fun GradleSpec.install() {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)
        installFixture(IncludedBuildConfiguredForHostingConventions)
        installFixture(TypesafeConventionsConfig) {
            ignorePackageNames = true
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
            customProjectFile("src/main/kotlin/com/example/someConvention.gradle.kts") {
                """
                package com.example    
                    
                println("Hello from someConvention")    
                """
            }
        }
    }
}
