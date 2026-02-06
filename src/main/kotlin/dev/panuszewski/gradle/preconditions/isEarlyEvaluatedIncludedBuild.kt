package dev.panuszewski.gradle.preconditions

import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal

internal fun Settings.isEarlyEvaluatedIncludedBuild(): Boolean =
    try {
        (gradle.parent as? GradleInternal)?.settings
        false
    } catch (_: IllegalStateException) {
        true
    }
