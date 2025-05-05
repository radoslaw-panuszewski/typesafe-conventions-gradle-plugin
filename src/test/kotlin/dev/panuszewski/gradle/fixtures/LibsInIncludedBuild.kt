package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.BuildConfigurator
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object LibsInIncludedBuild : NoConfigFixture {

    const val someLibrary = "org.apache.commons:commons-lang3:3.17.0"

    override fun install(spec: GradleSpec, includedBuild: BuildConfigurator, config: Unit) {
        with(spec) {

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
            }
        }
    }
}