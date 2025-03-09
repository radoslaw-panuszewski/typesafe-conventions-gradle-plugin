package dev.panuszewski.gradle.catalog

import dev.panuszewski.gradle.catalog.CatalogAccessorsPlugin.Companion.GENERATED_SOURCES_DIR
import dev.panuszewski.gradle.util.capitalizedName
import dev.panuszewski.gradle.util.createNewFile
import dev.panuszewski.gradle.util.readResourceAsString
import dev.panuszewski.gradle.util.typesafeConventions
import org.gradle.api.Project
import org.gradle.api.internal.catalog.DefaultVersionCatalog
import org.gradle.internal.management.VersionCatalogBuilderInternal
import org.gradle.kotlin.dsl.add
import java.io.File

internal object PluginCatalogAccessorsSupport {

    private const val MAIN_KOTLIN_SRC_DIR = "src/main/kotlin"
    private const val EXTRACTED_PLUGINS_BLOCKS_DIR = "kotlin-dsl/plugins-blocks/extracted"
    private val PLUGIN_DECLARATION_BY_ALIAS: Regex = """.*alias\(libs\.plugins\.(.+)\).*""".toRegex()

    fun apply(project: Project, catalog: VersionCatalogBuilderInternal) {
        val pluginDeclarations = collectPluginDeclarations(project, catalog)

        writeCatalogEntrypointForPluginBlock(project, catalog)
        patchPluginsBlocksBeforeCompilation(project, pluginDeclarations)

        if (project.typesafeConventions.autoPluginDependencies.get()) {
            addPluginMarkerDependencies(project, pluginDeclarations)
        }
    }

    private fun collectPluginDeclarations(project: Project, catalog: VersionCatalogBuilderInternal): List<PluginDeclaration> {
        val model = catalog.build()
        val srcDir = project.layout.projectDirectory.file(MAIN_KOTLIN_SRC_DIR).asFile

        return srcDir.walk()
            .filter { file -> file.name.endsWith(".gradle.kts") }
            .flatMap { file -> file.readText().lines() }
            .mapNotNull { line -> parsePluginDeclaration(line, model) }
            .toList()
    }

    private fun parsePluginDeclaration(line: String, model: DefaultVersionCatalog): PluginDeclaration? {
        val matchResult = PLUGIN_DECLARATION_BY_ALIAS.matchEntire(line)

        if (matchResult != null) {
            val alias = matchResult.groupValues[1]
            val aliasKebabCase = alias.replace(".", "-")
            val pluginModel = model.getPlugin(aliasKebabCase)

            return PluginDeclaration(
                pluginAlias = alias,
                pluginId = pluginModel.id,
                pluginVersion = pluginModel.version.toString()
            )
        }
        return null
    }

    private fun writeCatalogEntrypointForPluginBlock(project: Project, catalog: VersionCatalogBuilderInternal) {
        val source = readResourceAsString("/EntrypointForLibsInPluginsBlock.kt")
            .replace("libs", catalog.name)
            .replace("Libs", catalog.capitalizedName)

        val file = project.createNewFile("$GENERATED_SOURCES_DIR/EntrypointFor${catalog.capitalizedName}InPluginsBlock.kt")

        file.writeText(source)
    }

    private fun patchPluginsBlocksBeforeCompilation(project: Project, pluginDeclarations: List<PluginDeclaration>) {
        project.plugins.withId("org.gradle.kotlin.kotlin-dsl") {
            project.tasks.register("patchPluginsBlocks") {
                doLast {
                    val extractedPluginsBlocksDir =
                        project.layout.buildDirectory.file(EXTRACTED_PLUGINS_BLOCKS_DIR).get().asFile

                    extractedPluginsBlocksDir.walk()
                        .filter { file -> file.name.endsWith(".gradle.kts") }
                        .forEach { file -> patchPluginsBlock(file, pluginDeclarations) }
                }
            }
            project.tasks.findByName("compilePluginsBlocks")?.dependsOn("patchPluginsBlocks")
        }
    }

    private fun patchPluginsBlock(pluginsBlockFile: File, pluginDeclarations: List<PluginDeclaration>) {
        var content = pluginsBlockFile.readText()

        pluginDeclarations.forEach {
            content = content.replace(it.declarationByAlias, it.declarationById)
        }

        pluginsBlockFile.writeText(content)
    }

    private fun addPluginMarkerDependencies(project: Project, pluginDeclarations: List<PluginDeclaration>) {
        pluginDeclarations.forEach {
            project.dependencies.add("implementation", it.pluginMarkerWithoutVersion) {
                version { prefer(it.pluginVersion) }
            }
        }
    }
}

private data class PluginDeclaration(
    val pluginAlias: String,
    val pluginId: String,
    val pluginVersion: String
) {
    val declarationByAlias = "alias(libs.plugins.$pluginAlias)"
    val declarationById = "id(\"${pluginId}\")"
    val pluginMarkerWithoutVersion = "${pluginId}:${pluginId}.gradle.plugin"
}