package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object TopLevelBuild : NoConfigFixture {

    override fun GradleSpec.install() {
        installFixture(TypesafeConventionsAppliedToTopLevelBuild)

        libsVersionsToml {
            """
            [plugins]
            some-plugin = { id = "pl.allegro.tech.build.axion-release", version = "1.18.16" }
            
            [libraries]
            some-library = "org.apache.commons:commons-lang3:3.17.0"
            """
        }

        customProjectFile("src/main/kotlin/some-convention.gradle.kts") {
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
    }
}