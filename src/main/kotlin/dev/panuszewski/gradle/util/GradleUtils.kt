package dev.panuszewski.gradle.util

import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.dsl.VersionCatalogBuilder
import org.gradle.util.GradleVersion

internal fun gradleVersion(version: String): GradleVersion =
    GradleVersion.version(version)

internal fun Settings.gradleVersionAtLeast(version: String): Boolean =
    gradleVersion(gradle.gradleVersion) >= gradleVersion(version)

internal val VersionCatalogBuilder.capitalizedName: String
    get() = name.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }