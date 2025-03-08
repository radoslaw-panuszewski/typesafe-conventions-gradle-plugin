package dev.panuszewski.gradle

import dev.panuszewski.gradle.util.BaseGradleSpec
import dev.panuszewski.gradle.util.IncludedBuildConfigurator

fun BaseGradleSpec.accessorUsedInConventionPlugin(
    someLibrary: String,
    includedBuildForConventionPlugins: IncludedBuildConfigurator
) {
    customProjectFile("gradle/libs.versions.toml") {
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
        
        repositories {
            mavenCentral()
        }
        """
    }

    includedBuildForConventionPlugins {
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

fun BaseGradleSpec.accessorUsedInPluginsBlockOfConventionPlugin(
    somePlugin: String,
    somePluginVersion: String,
    includedBuildForConventionPlugins: IncludedBuildConfigurator
) {
    customProjectFile("gradle/libs.versions.toml") {
        """
        [plugins]
        some-plugin = { id = "$somePlugin", version = "$somePluginVersion" }
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

    includedBuildForConventionPlugins {
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