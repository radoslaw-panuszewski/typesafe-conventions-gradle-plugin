package dev.panuszewski.gradle.conventioncatalogs

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import java.io.File

internal class ConventionCatalogsPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        val conventionPlugins = collectConventionPlugins(settings)

        settings.dependencyResolutionManagement {
            versionCatalogs {
                create("conventions") {
                    for (conventionPlugin in conventionPlugins) {
                        plugin(conventionPlugin.pluginAlias, conventionPlugin.pluginId).version("")
                    }
                }
            }
        }
    }

    private fun collectConventionPlugins(settings: Settings): List<ConventionPlugin> =
        settings.rootDir.walk()
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
