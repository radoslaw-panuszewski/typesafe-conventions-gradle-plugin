@file:Suppress("UnstableApiUsage")

package dev.panuszewski.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.plugin.use.PluginDependency

public fun Project.pluginMarker(provider: Provider<PluginDependency>): Dependency {
    val plugin = provider.get()
    return dependencyWithRichVersion(
        group = plugin.pluginId,
        name = "${plugin.pluginId}.gradle.plugin",
        versionConstraint = plugin.version
    )
}

private fun Project.dependencyWithRichVersion(group: String, name: String, versionConstraint: VersionConstraint) =
    dependencies.create("$group:$name") {
        version {
            versionConstraint.strictVersion.takeIf(String::isNotBlank)?.let(::strictly)
            versionConstraint.requiredVersion.takeIf(String::isNotBlank)?.let(::require)
            versionConstraint.preferredVersion.takeIf(String::isNotBlank)?.let(::prefer)
        }
    }
