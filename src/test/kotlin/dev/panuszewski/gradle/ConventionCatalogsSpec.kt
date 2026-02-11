package dev.panuszewski.gradle

import dev.panuszewski.gradle.fixtures.ConventionCatalogUsedFromConventionPlugin
import dev.panuszewski.gradle.fixtures.ConventionCatalogUsedInParentBuild
import dev.panuszewski.gradle.fixtures.ConventionCatalogUsedInParentBuildThatIsNotRootBuild
import dev.panuszewski.gradle.fixtures.ConventionCatalogUsedInRootBuildThatIsNotDirectParent
import dev.panuszewski.gradle.fixtures.TypesafeConventionsAppliedToIncludedBuild
import dev.panuszewski.gradle.fixtures.includedbuild.BuildLogic
import dev.panuszewski.gradle.framework.BuildOutcome.BUILD_FAILED
import dev.panuszewski.gradle.framework.BuildOutcome.BUILD_SUCCESSFUL
import dev.panuszewski.gradle.framework.GradleSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

class ConventionCatalogsSpec : GradleSpec() {

    @Test
    fun `should generate typesafe accessor for convention plugin and use it from parent build`() {
        // given
        installFixture(BuildLogic)
        installFixture(ConventionCatalogUsedInParentBuild)

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain "Hello from someConvention"
    }

    @Test
    fun `should generate typesafe accessor for convention plugin and use it from parent build in multi-level hierarchy`() {
        // given
        installFixture(ConventionCatalogUsedInParentBuildThatIsNotRootBuild)

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain "Hello from someConvention"
    }

    /**
     * It would be problematic in the following hierarchy:
     * - build A includes B1 and B2
     * - build B1 includes build-logic
     * - build B2 includes build-logic
     *
     * (root build would receive convention catalog with conflicting declarations)
     */
    @Test
    fun `should not add convention catalog to root build in multi-level hierarchy`() {
        // given
        installFixture(ConventionCatalogUsedInRootBuildThatIsNotDirectParent)

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_FAILED
        result.output shouldContain "Unresolved reference 'conventions'"
    }

    @Test
    fun `should generate typesafe accessor for convention plugin and use it from another convention plugin`() {
        // given
        installFixture(BuildLogic)
        installFixture(ConventionCatalogUsedFromConventionPlugin)

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain "Hello from anotherConvention"
    }

    @Test
    fun `should allow custom name for convention catalog`() {
        // given
        installFixture(BuildLogic)
        installFixture(TypesafeConventionsAppliedToIncludedBuild)

        buildGradleKts {
            """
            plugins {
                alias(myConventions.plugins.someConvention)
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
                    gradlePluginPortal()
                }
                """
            }

            customProjectFile("src/main/kotlin/myConventions/someConvention.gradle.kts") {
                """
                package myConventions
                    
                println("Hello from someConvention")    
                """
            }
        }

        // when
        val result = runGradle()

        // then
        result.buildOutcome shouldBe BUILD_SUCCESSFUL
        result.output shouldContain "Hello from someConvention"
    }
}
