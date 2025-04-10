package dev.panuszewski.gradle

import dev.panuszewski.gradle.catalog.CatalogAccessorsPlugin
import dev.panuszewski.gradle.util.currentGradleVersion
import dev.panuszewski.gradle.util.gradleVersionAtLeast
import dev.panuszewski.gradle.util.typesafeConventions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logging
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.support.serviceOf
import java.io.File

@Suppress("unused") // used as plugin implementation class
internal class TypesafeConventionsPlugin : Plugin<Any> {

    override fun apply(target: Any) {
        require(gradleVersionAtLeast(MINIMAL_GRADLE_VERSION)) { mustUseMinimalGradleVersion() }

        val settings = target as? Settings ?: mustBeAppliedToSettings(target)
        registerExtension(settings)

        val parentBuild = settings.gradle.parent as? GradleInternal

        if (parentBuild != null) {
            inheritCatalogsFromParentBuild(settings, parentBuild)
        } else {
            failIfTopLevelBuildIsNotAllowed(settings)
        }
        enableCatalogAccessorsForAllProjects(settings)
    }

    private fun registerExtension(settings: Settings) {
        settings.extensions.create<TypesafeConventionsExtension>("typesafeConventions")
    }

    private fun inheritCatalogsFromParentBuild(settings: Settings, parentBuild: GradleInternal) {
        val parentGradleDir = resolveParentGradleDir(parentBuild, settings)
        val tomlFiles = discoverTomlFiles(parentGradleDir)
        createVersionCatalogs(tomlFiles, settings)
    }

    private fun resolveParentGradleDir(parentBuild: GradleInternal, settings: Settings): File =
        try {
            parentBuild.settings.rootDir.resolve("gradle")
        } catch (e: Throwable) {
            settings.rootDir.resolve("..").resolve("gradle")
        }

    private fun discoverTomlFiles(parentGradleDir: File): List<File> {
        val tomlFiles = parentGradleDir
            .walk()
            .filter { it.name.endsWith(".versions.toml") }
            .toList()

        if (tomlFiles.isEmpty()) {
            logger.warn("No version catalog TOML files found in the parent build (looked in $parentGradleDir)")
        }
        return tomlFiles
    }

    private fun createVersionCatalogs(tomlFiles: List<File>, settings: Settings) {
        val fileOperations = settings.serviceOf<FileOperations>()

        tomlFiles.forEach { tomlFile ->
            val catalogName = tomlFile.name.substringBefore(".versions.toml")

            settings.dependencyResolutionManagement.versionCatalogs {
                create(catalogName) {
                    from(fileOperations.configurableFiles(tomlFile))
                }
            }
        }
    }

    private fun enableCatalogAccessorsForAllProjects(target: Settings) {
        target.gradle.rootProject {
            allprojects {
                apply<CatalogAccessorsPlugin>()
            }
        }
    }

    private fun mustBeAppliedToSettings(target: Any): Nothing {
        val buildFileKind = when (target) {
            is Project -> "build.gradle.kts"
            is Gradle -> "init script"
            else -> target::class.simpleName
        }
        error(
            "The typesafe-conventions plugin must be applied to settings.gradle.kts, " +
                "but attempted to apply it to $buildFileKind"
        )
    }

    // TODO use GradlePluginApiVersion attribute instead
    private fun mustUseMinimalGradleVersion(): Nothing {
        error(
            "The typesafe-conventions plugin requires Gradle version at least $MINIMAL_GRADLE_VERSION, " +
                "but currently ${currentGradleVersion()} is used."
        )
    }

    private fun failIfTopLevelBuildIsNotAllowed(settings: Settings) {
        settings.gradle.settingsEvaluated {
            if (!typesafeConventions.allowTopLevelBuild.get()) {
                error(
                    """
                    The typesafe-conventions plugin is applied to a top-level build, but in most cases it should be applied to an included build or buildSrc. If you know what you're doing, allow top-level build in your settings.gradle.kts:
        
                    typesafeConventions { 
                        allowTopLevelBuild = true 
                    }
        
                    Read more here: https://github.com/radoslaw-panuszewski/typesafe-conventions-gradle-plugin/blob/main/README.md#top-level-build
                    """.trimIndent()
                )
            }
        }
    }

    companion object {
        private val logger = Logging.getLogger(TypesafeConventionsPlugin::class.java)
        internal const val MINIMAL_GRADLE_VERSION = "8.7"
    }
}

