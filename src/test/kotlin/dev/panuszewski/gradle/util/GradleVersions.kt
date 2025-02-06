package dev.panuszewski.gradle.util

import org.gradle.util.GradleVersion

object GradleVersions {
    val GRADLE_VERSION_TO_TEST = GradleVersion.version(System.getenv("GRADLE_VERSION_TO_TEST"))
}