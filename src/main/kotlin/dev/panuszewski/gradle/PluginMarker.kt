package dev.panuszewski.gradle

import org.gradle.api.provider.Provider
import org.gradle.plugin.use.PluginDependency

public fun pluginMarker(provider: Provider<PluginDependency>): String {
    val plugin = provider.get()
    return "${plugin.pluginId}:${plugin.pluginId}.gradle.plugin:${plugin.version}"
}