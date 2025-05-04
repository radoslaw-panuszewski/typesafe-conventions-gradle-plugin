package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.conventionPluginAppliedInRootProject

class MultipleCatalogsInDependenciesBlock : TestFixture() {

    val someLibrary = "org.apache.commons:commons-lang3:3.17.0"
    val anotherLibrary = "org.apache.commons:commons-collections4:4.4"

    override fun installFixture(): MultipleCatalogsInDependenciesBlock {
        spec.libsVersionsToml {
            """
            [libraries]
            some-library = "$someLibrary"
            """
        }

        spec.customProjectFile("gradle/tools.versions.toml") {
            """
            [libraries]
            another-library = "$anotherLibrary"
            """
        }

        spec.settingsGradleKts {
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

        spec.conventionPluginAppliedInRootProject(includedBuild) {
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

        return this
    }
}