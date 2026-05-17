package dev.panuszewski.gradle.util

import dev.panuszewski.gradle.TypesafeConventionsExtension
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.SettingsInternal
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.util.GradleVersion
import org.gradle.util.Path
import java.io.File

internal const val GENERATED_SOURCES_DIR = "generated-sources/typesafe-conventions/kotlin"
internal const val AUXILIARY_BUILD_DIR = "typesafe-conventions"

internal fun Project.fileInGeneratedSourcesDir(path: String): Provider<RegularFile> =
    layout.buildDirectory
        .dir(GENERATED_SOURCES_DIR)
        .map { it.file(path) }

internal fun Project.fileInAuxiliaryBuildDir(path: String): Provider<RegularFile> =
    layout.buildDirectory
        .dir(AUXILIARY_BUILD_DIR)
        .map { it.file(path) }

internal fun gradleVersion(version: String): GradleVersion =
    GradleVersion.version(version)

internal fun currentGradleVersion(): GradleVersion =
    GradleVersion.current()

internal fun gradleVersionAtLeast(version: String): Boolean =
    currentGradleVersion() >= gradleVersion(version)

internal val Project.settings: SettingsInternal
    get() = (project.gradle as GradleInternal).settings

internal val Project.typesafeConventions: TypesafeConventionsExtension
    get() = project.settings.typesafeConventions

internal val Settings.typesafeConventions: TypesafeConventionsExtension
    get() = extensions.getByType()

internal val Project.sourceSets: SourceSetContainer
    get() = extensions.getByName("sourceSets") as SourceSetContainer

internal val SourceSet.kotlin: SourceDirectorySet
    get() = extensions.getByName("kotlin") as SourceDirectorySet

/**
 * The [Path.getPath] is deprecated since Gradle 9.2.0,
 * but the new method [Path.asString] is not available in Gradle 8.7
 */
@Suppress("DEPRECATION")
internal val Path.pathString: String
    get() = path

/**
 * In 9.2.0, Gradle introduced an ABI-incompatible change for [SettingsInternal.getProjectRegistry] method,
 * so the code compiled against Gradle API >= 9.2.0 will fail on Gradle < 9.2.0.
 *
 * Because of that, we fall back to reflection on older Gradle versions.
 */
internal fun SettingsInternal.projectDirs(): List<File> =
    try {
        if (gradleVersionAtLeast("9.2.0")) {
            projectRegistry.allProjects.map { it.projectDir }
        } else {
            val projectRegistry = javaClass.getMethod("getProjectRegistry").invoke(this)
            val allProjects = projectRegistry.javaClass.getMethod("getAllProjects").invoke(projectRegistry) as Collection<*>
            allProjects.filterNotNull().map { it.javaClass.getMethod("getProjectDir").invoke(it) as File }
        }
    } catch (e: Exception) {
        logger.warn(
            """
            Unable to get projects dirs for '${rootProject.name}' build, convention catalog will scan all directories in $rootDir, which may impact performance. 
            Reason: ${e.message} 
            
            This is a bug, please report it here -> https://github.com/radoslaw-panuszewski/typesafe-conventions-gradle-plugin/issues
            """.trimIndent(),
        )
        emptyList()
    }

private val logger = Logging.getLogger("GradleUtils")
