package dev.panuszewski.gradle

import dev.panuszewski.gradle.util.AppendableFile
import dev.panuszewski.gradle.util.BaseGradleSpec
import dev.panuszewski.gradle.util.BuildConfigurator

fun BaseGradleSpec.conventionPluginAppliedInRootProject(
    includedBuild: BuildConfigurator,
    conventionPluginConfigurator: AppendableFile.() -> Any
) {
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
        customProjectFile("src/main/kotlin/some-convention.gradle.kts", conventionPluginConfigurator)

        buildGradleKts {
            """
            plugins {
                `kotlin-dsl`
            } 
            
            repositories {
                gradlePluginPortal()
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

fun BaseGradleSpec.multipleCatalogsInPluginsBlock(
    somePluginId: String,
    somePluginVersion: String,
    anotherPluginId: String,
    anotherPluginVersion: String,
    includedBuild: BuildConfigurator
) {
    libsVersionsToml {
        """
        [plugins]
        some-plugin = { id = "$somePluginId", version = "$somePluginVersion" }
        """
    }

    customProjectFile("gradle/tools.versions.toml") {
        """
        [plugins]
        another-plugin = { id = "$anotherPluginId", version = "$anotherPluginVersion" }
        """
    }

    settingsGradleKts {
        append {
            """
            dependencyResolutionManagement {
                versionCatalogs {
                    create("tools") {
                        from(files("gradle/tools.versions.toml"))
                    }
                }
            }
            """
        }
    }

    conventionPluginAppliedInRootProject(includedBuild) {
        """
        plugins {
            alias(libs.plugins.some.plugin)
            alias(tools.plugins.another.plugin)
        }
        """
    }
}