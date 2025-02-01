package dev.panuszewski.gradle

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logging
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.support.serviceOf
import java.io.File

@Suppress("unused") // used as plugin implementation class
internal class TypesafeConventionsPlugin : Plugin<Any> {

    override fun apply(target: Any) {
        val settings = target as? Settings ?: mustBeAppliedToSettings(target)
        val parentBuild = settings.gradle.parent ?: mustBeAppliedToIncludedBuild()

        useVersionCatalogsFromParentBuild(target, parentBuild)
        enableCatalogAccessorsForAllProjects(target)
    }

    private fun useVersionCatalogsFromParentBuild(settings: Settings, parentBuild: Gradle) {
        val parentBuildDir = parentBuild.startParameter.currentDir
        val parentGradleDir = parentBuildDir.resolve("gradle")
        val fileOperations = settings.serviceOf<FileOperations>()

        parentGradleDir
            .walk()
            .map { it.name }
            .filter { it.endsWith(".versions.toml") }
            .forEach { tomlFileName ->
                val catalogName = tomlFileName.substringBefore(".versions.toml")
                val tomlFileLocation = parentGradleDir.resolve(tomlFileName)

                settings.dependencyResolutionManagement.versionCatalogs {
                    create(catalogName) {
                        from(fileOperations.configurableFiles(tomlFileLocation))
                    }
                }
            }
    }

    private fun enableCatalogAccessorsForAllProjects(target: Settings) {
        target.gradle.rootProject {
            allprojects {
                apply<VersionCatalogAccessorsPlugin>()
            }
        }
    }

    private fun mustBeAppliedToIncludedBuild(): Nothing {
        error(
            "The typesafe-conventions plugin can only be applied to buildSrc " +
                "or build-logic included build, but attempted to apply it to top-level build"
        )
    }

    private fun mustBeAppliedToSettings(target: Any): Nothing {
        error(
            "The typesafe-conventions plugin must be applied to Settings, " +
                "but attempted to apply it to ${target::class.simpleName}"
        )
    }

    companion object {
        private val logger = Logging.getLogger(TypesafeConventionsPlugin::class.java)
    }
}

