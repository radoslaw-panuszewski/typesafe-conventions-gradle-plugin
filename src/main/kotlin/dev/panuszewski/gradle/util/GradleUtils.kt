package dev.panuszewski.gradle.util

import dev.panuszewski.gradle.TypesafeConventionsExtension
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.SettingsInternal
import org.gradle.kotlin.dsl.getByType
import org.gradle.util.GradleVersion

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
