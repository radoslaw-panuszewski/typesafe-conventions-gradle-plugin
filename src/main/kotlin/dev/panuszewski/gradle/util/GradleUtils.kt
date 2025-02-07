package dev.panuszewski.gradle.util

import org.gradle.api.initialization.Settings
import org.gradle.util.GradleVersion

internal fun gradleVersion(version: String): GradleVersion =
    GradleVersion.version(version)

internal fun Settings.gradleVersionAtLeast(version: String): Boolean =
    gradleVersion(gradle.gradleVersion) >= gradleVersion(version)