package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object MultiLevelBuildHierarchy : NoConfigFixture {

    val someLibrary = "org.apache.commons:commons-lang3:3.17.0"

    override fun GradleSpec.install() {
        val secondaryBuild = mainBuild.registerIncludedBuild("secondary-build")
        val buildLogic = secondaryBuild.registerIncludedBuild("build-logic")

        with(mainBuild) {
            buildGradleKts {
                """
                plugins {
                    java
                }
                
                dependencies {
                    implementation("com.example:secondary-build:1.0.0")
                }
                """
            }
        }

        with(secondaryBuild) {
            libsVersionsToml {
                """
                [libraries]
                some-library = "$someLibrary"    
                """
            }

            buildGradleKts {
                """
                plugins {
                    id("some-convention")
                }
                
                group = "com.example"
                
                repositories {
                    mavenCentral()
                }
                """
            }
        }

        with(buildLogic) {
            settingsGradleKts {
                """
                pluginManagement {
                    repositories {
                        gradlePluginPortal()
                        mavenLocal()
                    }
                }
                    
                plugins {
                    id("dev.panuszewski.typesafe-conventions") version "$projectVersion"
                }
                """
            }

            buildGradleKts {
                """
                plugins {
                    `kotlin-dsl`
                } 
                
                repositories {
                    mavenCentral()
                }
                """
            }

            customProjectFile("src/main/kotlin/some-convention.gradle.kts") {
                """
                plugins {
                    java
                }
                
                dependencies {
                    implementation(libs.some.library)
                }    
                """
            }
        }
    }
}
