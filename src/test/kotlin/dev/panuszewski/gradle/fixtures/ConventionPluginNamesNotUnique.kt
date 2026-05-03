package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object ConventionPluginNamesNotUnique : NoConfigFixture {

    override fun GradleSpec.install() {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)
        installFixture(IncludedBuildConfiguredForHostingConventions)

        buildGradleKts {
            """
            plugins {
                alias(conventions.plugins.com.first.someConvention)
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
