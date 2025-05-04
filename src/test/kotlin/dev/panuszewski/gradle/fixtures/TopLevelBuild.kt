package dev.panuszewski.gradle.fixtures

class TopLevelBuild : TestFixture() {

    override fun installFixture(): TopLevelBuild {
        spec.libsVersionsToml {
            """
            [plugins]
            some-plugin = { id = "pl.allegro.tech.build.axion-release", version = "1.18.16" }
            
            [libraries]
            some-library = "org.apache.commons:commons-lang3:3.17.0"
            """
        }

        spec.customProjectFile("src/main/kotlin/some-convention.gradle.kts") {
            """
            plugins {
                java
                alias(libs.plugins.some.plugin)
            }
            
            dependencies {
                implementation(libs.some.library)
            }
            """
        }

        spec.buildGradleKts {
            """
            plugins {
                `kotlin-dsl`
            }
            
            repositories {
                gradlePluginPortal()
            }
            """
        }

        spec.settingsGradleKts {
            """
            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    mavenLocal()
                }
            }
                
            plugins {
                id("dev.panuszewski.typesafe-conventions") version "${spec.projectVersion}"
            }
            
            typesafeConventions { 
                allowTopLevelBuild = true 
            }
            """
        }

        return this
    }
}