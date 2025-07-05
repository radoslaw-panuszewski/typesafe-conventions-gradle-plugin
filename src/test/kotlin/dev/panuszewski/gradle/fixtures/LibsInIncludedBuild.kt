package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object LibsInIncludedBuild : NoConfigFixture {

    const val someLibrary = "org.apache.commons:commons-lang3:3.17.0"

    override fun GradleSpec.install() {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)

        libsVersionsToml {
            """
            [libraries]
            some-library = "$someLibrary"
            """
        }

        buildGradleKts {
            """
            plugins {
                java
            }
            """
        }

        includedBuild {
            buildGradleKts {
                """
                plugins {
                    `kotlin-dsl`
                } 
                
                repositories {
                    mavenCentral()
                }
                
                dependencies {
                    implementation(libs.some.library)
                }
                """
            }
        }
    }
}