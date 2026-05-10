package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object IgnorePackageNamesNotUnique : NoConfigFixture {

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
            customProjectFile("src/main/kotlin/com/first/someConvention.gradle.kts") {
                """
                package com.first    
                    
                println("Hello from first someConvention")    
                """
            }

            customProjectFile("src/main/kotlin/com/second/someConvention.gradle.kts") {
                """
                package com.second    
                    
                println("Hello from second someConvention")    
                """
            }
        }
    }
}
