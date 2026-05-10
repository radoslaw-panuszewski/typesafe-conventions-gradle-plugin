package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object ConventionCatalogUsedInRootBuildThatIsNotDirectParent : NoConfigFixture {

    override fun GradleSpec.install() {
        val secondaryBuild = mainBuild.registerIncludedBuild("secondary-build")
        val buildLogic = secondaryBuild.registerIncludedBuild("build-logic")

        installFixture(TypesafeConventionsAppliedToIncludedBuild) { build = buildLogic }
        installFixture(IncludedBuildConfiguredForHostingConventions) { build = buildLogic }

        with(mainBuild) {
            buildGradleKts {
                """
                plugins {
                    alias(conventions.plugins.someConvention)
                }
                """
            }
        }

        with(buildLogic) {
            customProjectFile("src/main/kotlin/someConvention.gradle.kts") {
                """
                println("Hello from someConvention")
                """
            }
        }
    }
}
