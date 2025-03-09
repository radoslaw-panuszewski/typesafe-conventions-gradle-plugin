package dev.panuszewski.gradle

import dev.panuszewski.gradle.util.BaseGradleSpec
import dev.panuszewski.gradle.util.BuildConfigurator

fun BaseGradleSpec.accessorUsedInConventionPlugin(library: String, includedBuild: BuildConfigurator) {
    customProjectFile("gradle/libs.versions.toml") {
        """
        [libraries]
        some-library = "$library"
        """
    }

    buildGradleKts {
        """
        plugins {
            id("some-convention")
        }
        
        repositories {
            mavenCentral()
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
                id("dev.panuszewski.typesafe-conventions") version "${System.getenv("PROJECT_VERSION")}"
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

fun BaseGradleSpec.accessorUsedInPluginsBlockOfConventionPlugin(pluginId: String, pluginVersion: String, includedBuild: BuildConfigurator) {
    customProjectFile("gradle/libs.versions.toml") {
        """
        [plugins]
        some-plugin = { id = "$pluginId", version = "$pluginVersion" }
        """
    }

    buildGradleKts {
        """
        plugins {
            id("some-convention")
        }
        
        repositories {
            mavenCentral()
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
                id("dev.panuszewski.typesafe-conventions") version "${System.getenv("PROJECT_VERSION")}"
            }
            """
        }

        customProjectFile("src/main/kotlin/some-convention.gradle.kts") {
            """
            plugins {
                alias(libs.plugins.some.plugin)
            }
            """
        }
    }
}