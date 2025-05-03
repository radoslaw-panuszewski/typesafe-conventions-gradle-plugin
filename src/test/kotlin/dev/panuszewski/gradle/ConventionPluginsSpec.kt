package dev.panuszewski.gradle

import dev.panuszewski.gradle.util.BaseGradleSpec
import dev.panuszewski.gradle.util.BuildOutcome.BUILD_FAILED
import dev.panuszewski.gradle.util.BuildOutcome.BUILD_SUCCESSFUL
import dev.panuszewski.gradle.util.BuildConfigurator
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ConventionPluginsSpec : BaseGradleSpec() {

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should allow to use catalog accessors in convention plugin`(includedBuild: BuildConfigurator) {
        // given
        val library = "org.apache.commons:commons-lang3:3.17.0"
        libsInDependenciesBlock(library, includedBuild)

        // when
        val result = runGradle("dependencyInsight", "--dependency", library)

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain library
        result.output shouldNotContain "$library FAILED"
    }

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should allow to use catalog accessors in plugins block of convention plugin`(includedBuild: BuildConfigurator) {
        // given
        val pluginId = "pl.allegro.tech.build.axion-release"
        val pluginVersion = "1.18.16"
        val taskRegisteredByPlugin = "verifyRelease"

        // and
        libsInPluginsBlock(pluginId, pluginVersion, includedBuild)

        // when
        val result = runGradle("tasks")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain taskRegisteredByPlugin
    }

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should respect disabling accessors in plugins block`(includedBuild: BuildConfigurator) {
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion >= GradleVersion.version("8.8"))

        // given
        val pluginId = "pl.allegro.tech.build.axion-release"
        val pluginVersion = "1.18.16"

        // and
        libsInPluginsBlock(pluginId, pluginVersion, includedBuild)

        // and
        includedBuild {
            settingsGradleKts {
                append {
                    """
                    typesafeConventions {
                        accessorsInPluginsBlock = false
                    }
                    """
                }
            }
        }

        // when
        val result = runGradle("help")

        // then
        result.buildOutcome shouldBe BUILD_FAILED
        result.output shouldContain "Unresolved reference: libs"
    }

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should respect disabling accessors in plugins block in old Gradle`(includedBuild: BuildConfigurator) {
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion < GradleVersion.version("8.8"))

        // given
        val pluginId = "pl.allegro.tech.build.axion-release"
        val pluginVersion = "1.18.16"

        // and
        libsInPluginsBlock(pluginId, pluginVersion, includedBuild)

        // and
        includedBuild {
            settingsGradleKts {
                prepend {
                    """
                    import dev.panuszewski.gradle.TypesafeConventionsExtension
                    """
                }
                append {
                    """
                    configure<TypesafeConventionsExtension> {
                        accessorsInPluginsBlock = false
                    }    
                    """
                }
            }
        }

        // when
        val result = runGradle("help")

        // then
        result.buildOutcome shouldBe BUILD_FAILED
        result.output shouldContain "Unresolved reference: libs"
    }

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should respect disabling auto plugin dependencies`(includedBuild: BuildConfigurator) {
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion >= GradleVersion.version("8.8"))

        // given
        val pluginId = "pl.allegro.tech.build.axion-release"
        val pluginVersion = "1.18.16"

        // and
        libsInPluginsBlock(pluginId, pluginVersion, includedBuild)

        // and
        includedBuild {
            settingsGradleKts {
                append {
                    """
                    typesafeConventions {
                        autoPluginDependencies = false
                    }
                    """
                }
            }
        }

        // when
        val result = runGradle("help")

        // then
        result.buildOutcome shouldBe BUILD_FAILED
        result.output shouldContain "Plugin [id: '$pluginId'] was not found in any of the following sources"
    }

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should respect disabling auto plugin dependencies in old Gradle`(includedBuild: BuildConfigurator) {
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion < GradleVersion.version("8.8"))

        // given
        val pluginId = "pl.allegro.tech.build.axion-release"
        val pluginVersion = "1.18.16"

        // and
        libsInPluginsBlock(pluginId, pluginVersion, includedBuild)

        // and
        includedBuild {
            settingsGradleKts {
                prepend {
                    """
                    import dev.panuszewski.gradle.TypesafeConventionsExtension
                    """
                }
                append {
                    """
                    configure<TypesafeConventionsExtension> {
                        autoPluginDependencies = false
                    }
                    """
                }
            }
        }

        // when
        val result = runGradle("help")

        // then
        result.buildOutcome shouldBe BUILD_FAILED
        result.output shouldContain "Plugin [id: '$pluginId'] was not found in any of the following sources"
    }

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should allow to override auto plugin dependency`(includedBuild: BuildConfigurator) {
        // given
        val pluginId = "pl.allegro.tech.build.axion-release"
        val pluginMarker = "$pluginId:$pluginId.gradle.plugin"
        val pluginVersion = "1.18.16"
        val overriddenPluginVersion = "1.18.15"

        // and
        libsInPluginsBlock(pluginId, pluginVersion, includedBuild)

        // and
        includedBuild {
            buildGradleKts {
                prepend {
                    """
                    import dev.panuszewski.gradle.TypesafeConventionsExtension    
                    """
                }
                append {
                    """
                    dependencies {
                        implementation("$pluginMarker:$overriddenPluginVersion")
                    }
                    """
                }
            }
        }

        // when
        val buildName = includedBuilds.keys.first().substringAfterLast("/")
        val result = runGradle(":$buildName:dependencyInsight", "--dependency", pluginMarker)

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain "dependencyInsight${System.lineSeparator()}$pluginMarker:$overriddenPluginVersion"
    }

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should support multiple catalogs`(includedBuild: BuildConfigurator) {
        // given
        val someLibrary = "org.apache.commons:commons-lang3:3.17.0"
        val anotherLibrary = "org.apache.commons:commons-collections4:4.4"

        // and
        multipleCatalogsInDependenciesBlock(someLibrary, anotherLibrary, includedBuild)

        // when
        val someLibraryResult = runGradle("dependencyInsight", "--dependency", someLibrary)
        val anotherLibraryResult = runGradle("dependencyInsight", "--dependency", anotherLibrary)

        // then
        someLibraryResult.buildOutcome shouldBe BUILD_SUCCESSFUL
        someLibraryResult.output shouldContain someLibrary
        someLibraryResult.output shouldNotContain "$someLibrary FAILED"

        anotherLibraryResult.buildOutcome shouldBe BUILD_SUCCESSFUL
        anotherLibraryResult.output shouldContain anotherLibrary
        anotherLibraryResult.output shouldNotContain "$anotherLibrary FAILED"
    }

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should support multiple catalogs in plugins block`(includedBuild: BuildConfigurator) {
        // given
        val somePluginId = "pl.allegro.tech.build.axion-release"
        val somePluginVersion = "1.18.16"
        val taskRegisteredBySomePlugin = "verifyRelease"

        val anotherPluginId = "com.github.ben-manes.versions"
        val anotherPluginVersion = "0.52.0"
        val taskRegisteredByAnotherPlugin = "dependencyUpdates"

        // and
        multipleCatalogsInPluginsBlock(
            somePluginId = somePluginId,
            somePluginVersion = somePluginVersion,
            anotherPluginId = anotherPluginId,
            anotherPluginVersion = anotherPluginVersion,
            includedBuild = includedBuild
        )

        // when
        val result = runGradle("tasks")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain taskRegisteredBySomePlugin
        result.output shouldContain taskRegisteredByAnotherPlugin
    }

    @Test
    fun `should work for top-level build`() {
        // Gradle < 8.8 does not support typesafe extensions in settings.gradle.kts
        assumeTrue(gradleVersion >= GradleVersion.version("8.8"))

        // given
        customProjectFile("gradle/libs.versions.toml") {
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
            
            typesafeConventions { 
                allowTopLevelBuild = true 
            }
            """
        }

        // when
        val result = runGradle("assemble")

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
    }

    @ParameterizedTest
    @MethodSource("includedBuildConfigurators")
    fun `should support imported version catalogs`(includedBuild: BuildConfigurator) {
        // given
        val library = "org.apache.commons:commons-lang3:3.17.0"
        libsInDependenciesBlock(library, includedBuild)

        // and
        settingsGradleKts {
            append {
                """
                dependencyResolutionManagement {
                    repositories {
                        mavenCentral()
                    }
                
                    versionCatalogs {
                        create("mn") {
                            from("io.micronaut.platform:micronaut-platform:4.8.2")
                        }
                    }
                }
                """
            }
        }

        // and
        includedBuild {
            customProjectFile("src/main/kotlin/some-convention.gradle.kts") {
                """
                plugins {
                    java
                }
                
                dependencies {
                    implementation(mn.micronaut.core)
                }
                """
            }
        }

        // when
        val result = runGradle("dependencyInsight", "--dependency", library)

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain library
        result.output shouldNotContain "$library FAILED"
    }
}