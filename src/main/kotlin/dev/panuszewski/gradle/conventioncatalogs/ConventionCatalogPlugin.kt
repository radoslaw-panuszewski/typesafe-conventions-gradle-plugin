package dev.panuszewski.gradle.conventioncatalogs

import dev.panuszewski.gradle.util.typesafeConventions
import org.gradle.api.initialization.Settings
import java.io.File

internal object ConventionCatalogPlugin {

    fun apply(parentBuildSettings: Settings, includedBuildSettings: Settings) {
        val catalogName = includedBuildSettings.typesafeConventions.conventionCatalog.catalogName.get()
        val ignorePackageNames = includedBuildSettings.typesafeConventions.conventionCatalog.ignorePackageNames.get()

        val conventionPlugins = collectConventionPlugins(includedBuildSettings, ignorePackageNames)

        if (ignorePackageNames) {
            val duplicates = conventionPlugins.groupBy { it.pluginAlias }.filter { it.value.size > 1 }
            check(duplicates.isEmpty()) {
                "Found duplicated convention plugin names: ${duplicates.keys}. " +
                    "Either set typesafeConventions.conventionCatalog.ignorePackages = false, " +
                    "or make every convention plugin name unique."
            }
        }

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

    private fun collectConventionPlugins(includedBuildSettings: Settings, ignorePackages: Boolean): List<ConventionPlugin> =
        includedBuildSettings.rootDir.walk()
            .filter { file -> file.path.contains("src") && file.name.endsWith(".gradle.kts") }
            .map { parseConventionPlugin(it, ignorePackages) }
            .toList()

    private fun parseConventionPlugin(file: File, ignorePackages: Boolean): ConventionPlugin {
        val content = file.readText()
        val packageRegex = """^\s*package\s+([\w.]+)""".toRegex(RegexOption.MULTILINE)
        val packageName = packageRegex.find(content)?.groupValues?.get(1)
        val pluginName = file.name.removeSuffix(".gradle.kts")
        val pluginId = packageName?.let { "$it.$pluginName" } ?: pluginName
        val pluginAlias = if (ignorePackages) pluginName.replace(".", "-") else pluginId.replace(".", "-")
        return ConventionPlugin(pluginId, pluginAlias)
    }
}
