package dev.panuszewski.gradle.util

import dev.panuszewski.gradle.TypesafeConventionsExtension
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.dsl.VersionCatalogBuilder
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.SettingsInternal
import org.gradle.kotlin.dsl.getByType
import org.gradle.util.GradleVersion

internal fun gradleVersion(version: String): GradleVersion =
    GradleVersion.version(version)

internal fun Settings.gradleVersionAtLeast(version: String): Boolean =
    gradleVersion(gradle.gradleVersion) >= gradleVersion(version)

internal val VersionCatalogBuilder.capitalizedName: String
    get() = name.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }

internal val Project.settings: SettingsInternal
    get() = (project.gradle as GradleInternal).settings

internal val Project.typesafeConventions: TypesafeConventionsExtension
    get() = project.settings.extensions.getByType()
