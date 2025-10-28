package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object CyclicBuildHierarchy : NoConfigFixture {
    override fun GradleSpec.install() {
        val secondaryBuild = mainBuild.registerIncludedBuild("secondary-build")
        val buildLogic = mainBuild.registerIncludedBuild("build-logic")
        secondaryBuild.includeBuild("..")

        with(mainBuild) {
            buildGradleKts {
                """
                plugins {
                    java
                }
                """
            }
        }

        with(secondaryBuild) {
            buildGradleKts {
                """
                plugins {
                    java
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
        }
    }
}