package dev.panuszewski.gradle.conventioncatalogs

import dev.panuszewski.gradle.util.typesafeConventions
import org.gradle.api.initialization.Settings
import java.io.File

internal object ConventionCatalogPlugin {

    fun apply(parentBuildSettings: Settings, includedBuildSettings: Settings) {
        val catalogName = includedBuildSettings.typesafeConventions.conventionCatalog.catalogName.get()
        val conventionPlugins = collectConventionPlugins(includedBuildSettings)

        parentBuildSettings.dependencyResolutionManagement {
            versionCatalogs {
                create(catalogName) {
                    for (conventionPlugin in conventionPlugins) {
                        plugin(conventionPlugin.pluginAlias, conventionPlugin.pluginId).version("")
                    }
                }
            }
        }
    }

    private fun collectConventionPlugins(includedBuildSettings: Settings): List<ConventionPlugin> =
        includedBuildSettings.rootDir.walk()
            .filter { file -> file.path.contains("src") && file.name.endsWith(".gradle.kts") }
            .map(::parseConventionPlugin)
            .toList()

    private fun parseConventionPlugin(file: File): ConventionPlugin {
        val content = file.readText()
        val packageRegex = """^\s*package\s+([\w.]+)""".toRegex(RegexOption.MULTILINE)
        val packageName = packageRegex.find(content)?.groupValues?.get(1)
        val pluginName = file.name.removeSuffix(".gradle.kts")
        val pluginId = packageName?.let { "$it.$pluginName" } ?: pluginName
        val pluginAlias = pluginId.replace(".", "-")
        return ConventionPlugin(pluginId, pluginAlias)
    }
}
