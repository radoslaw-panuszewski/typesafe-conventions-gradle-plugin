package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.BuildConfigurator
import dev.panuszewski.gradle.framework.NoConfigFixture

object MultipleCatalogsInDependenciesBlock : NoConfigFixture {

    const val someLibrary = "org.apache.commons:commons-lang3:3.17.0"
    const val anotherLibrary = "org.apache.commons:commons-collections4:4.4"

    override fun GradleSpec.install() {
        installFixture(TypesafeConventionsAppliedToIncludedBuild)

        installFixture(ConventionPlugin) {
            pluginBody = """
                plugins {
                    java
                }
                
                dependencies {
                    implementation(libs.some.library)
                    implementation(tools.another.library)
                }
                """
        }

        libsVersionsToml {
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
    }
}