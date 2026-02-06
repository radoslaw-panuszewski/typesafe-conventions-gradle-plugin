package dev.panuszewski.gradle.conventioncatalogs

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import java.io.File

internal class ConventionCatalogsPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        val conventionPluginScripts = collectConventionPluginScripts(settings)

        val conventionsByCatalog = groupConventionsByCatalog(conventionPluginScripts)

        val parentSettings = (settings.gradle.parent?.gradle as? GradleInternal)?.settings

        parentSettings?.dependencyResolutionManagement {
            versionCatalogs {
                conventionsByCatalog.keys.forEach { catalog ->
                    create(catalog) {
                        conventionsByCatalog[catalog]?.forEach { conventionPlugin ->
                            plugin(conventionPlugin.pluginName, conventionPlugin.pluginId).version("")
                        }
                    }
                }
            }
        }
    }

    private fun collectConventionPluginScripts(settings: Settings): Set<File> =
        settings.rootDir.walk()
            .filter { file -> file.path.contains("src") && file.name.endsWith(".gradle.kts") }
            .toSet()

    private fun groupConventionsByCatalog(scriptFiles: Set<File>): Map<String, List<ConventionPlugin>> =
        scriptFiles
            .map(::createConventionPlugin)
            .groupBy { it.packageName ?: "conventions" }

    private fun createConventionPlugin(file: File): ConventionPlugin {
        val content = file.readText()
        val packageRegex = """^\s*package\s+([\w.]+)""".toRegex(RegexOption.MULTILINE)
        val packageName = packageRegex.find(content)?.groupValues?.get(1)
        val pluginName = file.name.removeSuffix(".gradle.kts")
        val pluginId = packageName?.let { "$it.$pluginName" } ?: "conventions.$pluginName"
        return ConventionPlugin(pluginName, pluginId, packageName)
    }
}
