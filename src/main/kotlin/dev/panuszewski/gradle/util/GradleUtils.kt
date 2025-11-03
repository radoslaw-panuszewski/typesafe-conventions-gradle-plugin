package dev.panuszewski.gradle.util

import dev.panuszewski.gradle.TypesafeConventionsExtension
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.SettingsInternal
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.util.GradleVersion
import org.gradle.util.Path

internal fun gradleVersion(version: String): GradleVersion =
    GradleVersion.version(version)

internal fun currentGradleVersion(): GradleVersion =
    GradleVersion.current()

internal fun gradleVersionAtLeast(version: String): Boolean =
    currentGradleVersion() >= gradleVersion(version)

/**
 * The [Path.getPath] is deprecated since Gradle 9.2.0,
 * but the new method [Path.asString] is not available in Gradle 8.7
 */
@Suppress("DEPRECATION")
internal val Path.pathString: String
    get() = path

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
