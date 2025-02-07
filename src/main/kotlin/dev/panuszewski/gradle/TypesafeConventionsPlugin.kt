package dev.panuszewski.gradle

import dev.panuszewski.gradle.util.gradleVersionAtLeast
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logging
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.support.serviceOf

@Suppress("unused") // used as plugin implementation class
internal class TypesafeConventionsPlugin : Plugin<Any> {

    override fun apply(target: Any) {
        val settings = target as? Settings ?: mustBeAppliedToSettings(target)
        val parentBuild = settings.gradle.parent ?: mustBeAppliedToIncludedBuild()
        require(settings.gradleVersionAtLeast(MINIMAL_GRADLE_VERSION)) { mustUseMinimalGradleVersion(settings) }

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

    private fun mustBeAppliedToSettings(target: Any): Nothing {
        val buildFileKind = when(target) {
            is Project -> "build.gradle.kts"
            is Gradle -> "init script"
            else -> target::class.simpleName
        }
        error(
            "The typesafe-conventions plugin must be applied to settings.gradle.kts, " +
                "but attempted to apply it to $buildFileKind"
        )
    }

    private fun mustBeAppliedToIncludedBuild(): Nothing {
        error(
            "The typesafe-conventions plugin must be applied to an included build, " +
                "but attempted to apply it to a top-level build"
        )
    }

    private fun mustUseMinimalGradleVersion(settings: Settings): Nothing {
        error(
            "The typesafe-conventions plugin requires Gradle version at least $MINIMAL_GRADLE_VERSION, " +
                "but currently Gradle ${settings.gradle.gradleVersion} is used."
        )
    }

    companion object {
        private val logger = Logging.getLogger(TypesafeConventionsPlugin::class.java)
        internal const val MINIMAL_GRADLE_VERSION = "8.4"
    }
}

