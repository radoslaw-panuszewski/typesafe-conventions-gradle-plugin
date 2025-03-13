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
        customProjectFile("src/main/kotlin/some-convention.gradle.kts") {
            """
            plugins {
                alias(libs.plugins.some.plugin)
            }
            """
        }

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

fun BaseGradleSpec.multipleVersionCatalogs(someLibrary: String, anotherLibrary: String, includedBuild: BuildConfigurator) {
    customProjectFile("gradle/libs.versions.toml") {
        """
        [libraries]
        some-library = "$someLibrary"
        """
    }

    customProjectFile("gradle/tools.versions.toml") {
        """
        [libraries]
        another-library = "$anotherLibrary"
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
        customProjectFile("src/main/kotlin/some-convention.gradle.kts") {
            """
            plugins {
                java
            }
            
            dependencies {
                implementation(libs.some.library)
                implementation(tools.another.library)
            }
            """
        }

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

fun BaseGradleSpec.multipleCatalogsInPluginsBlockOfConventionPlugin(
    somePluginId: String,
    somePluginVersion: String,
    anotherPluginId: String,
    anotherPluginVersion: String,
    includedBuild: BuildConfigurator
) {
    customProjectFile("gradle/libs.versions.toml") {
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
        customProjectFile("src/main/kotlin/some-convention.gradle.kts") {
            """
            plugins {
                alias(libs.plugins.some.plugin)
                alias(tools.plugins.another.plugin)
            }
            """
        }

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