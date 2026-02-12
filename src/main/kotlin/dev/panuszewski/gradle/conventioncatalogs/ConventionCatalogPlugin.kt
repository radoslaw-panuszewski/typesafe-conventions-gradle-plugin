package dev.panuszewski.gradle.conventioncatalogs

import dev.panuszewski.gradle.ConventionCatalogExtension
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.extra
import java.io.File

internal class ConventionCatalogPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        val conventionCatalogConfig = settings.extra[CONVENTION_CATALOG_CONFIG_KEY] as ConventionCatalogExtension
        val conventionPlugins = collectConventionPlugins(settings)

        settings.dependencyResolutionManagement {
            versionCatalogs {
                create(conventionCatalogConfig.catalogName.get()) {
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

    companion object {
        const val CONVENTION_CATALOG_CONFIG_KEY = "conventionCatalogConfig"
    }
}
