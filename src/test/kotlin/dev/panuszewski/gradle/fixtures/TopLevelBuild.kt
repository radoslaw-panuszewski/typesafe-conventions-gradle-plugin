package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.BuildConfigurator
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture
import dev.panuszewski.gradle.util.gradleVersion

object TopLevelBuild : NoConfigFixture {

    override fun GradleSpec.install(includedBuild: BuildConfigurator) {
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