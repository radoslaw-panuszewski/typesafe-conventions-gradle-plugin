package dev.panuszewski.gradle.catalog

import dev.panuszewski.gradle.catalog.CatalogAccessorsPlugin.Companion.GENERATED_SOURCES_DIR_RELATIVE
import dev.panuszewski.gradle.util.capitalized
import dev.panuszewski.gradle.util.typesafeConventions
import org.gradle.api.Project
import org.gradle.api.internal.catalog.DefaultVersionCatalog
import org.gradle.internal.management.VersionCatalogBuilderInternal
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.register
import java.io.File
import java.io.Serializable

internal object PluginCatalogAccessorsSupport {

    private const val MAIN_KOTLIN_SRC_DIR = "src/main/kotlin"
    private const val EXTRACTED_PLUGINS_BLOCKS_DIR = "kotlin-dsl/plugins-blocks/extracted"
    private val PLUGIN_DECLARATION_BY_ALIAS: Regex = """.*alias\(.+\.plugins\.(.+)\).*""".toRegex()

    fun apply(project: Project, catalog: VersionCatalogBuilderInternal) {
        val pluginDeclarations = collectPluginDeclarations(project, catalog)

        writeCatalogEntrypointBeforeCompilation(project, catalog)
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

    private fun parsePluginDeclaration(line: String, catalogModel: DefaultVersionCatalog): PluginDeclaration? {
        val matchResult = PLUGIN_DECLARATION_BY_ALIAS.matchEntire(line)

        if (matchResult != null) {
            val alias = matchResult.groupValues[1]
            val aliasKebabCase = alias.replace(".", "-")

            if (catalogModel.hasPlugin(aliasKebabCase)) {
                val pluginModel = catalogModel.getPlugin(aliasKebabCase)

                return PluginDeclaration(
                    pluginAlias = alias,
                    pluginId = pluginModel.id,
                    pluginVersion = pluginModel.version.toString(),
                    catalogName = catalogModel.name,
                )
            }
        }
        return null
    }

    private fun writeCatalogEntrypointBeforeCompilation(project: Project, catalog: VersionCatalogBuilderInternal) {
        val entrypointName = "EntrypointFor${catalog.name.capitalized}InPluginsBlock"

        val generateEntrypointTask = project.tasks.register<GenerateCatalogEntrypointTask>("generate$entrypointName") {
            this.catalogName.set(catalog.name)
            this.entrypointTemplateName.set("EntrypointForCatalogInPluginsBlock")
            this.outputFile.set(project.layout.buildDirectory.file("$GENERATED_SOURCES_DIR_RELATIVE/$entrypointName.kt"))
        }

        project.tasks.named("compileKotlin") {
            dependsOn(generateEntrypointTask)
        }
    }

    private fun patchPluginsBlocksBeforeCompilation(project: Project, pluginDeclarations: List<PluginDeclaration>) {
        project.plugins.withId("org.gradle.kotlin.kotlin-dsl") {
            // we add action to existing task instead of registering a dedicated task to allow caching
            // (otherwise the dedicated task would modify its own input and never be UP-TO-DATE)
            project.tasks.findByName("extractPrecompiledScriptPluginPlugins")
                ?.doLast {
                    project.layout.buildDirectory.dir(EXTRACTED_PLUGINS_BLOCKS_DIR).get().asFile.walk()
                        .filter { file -> file.name.endsWith(".gradle.kts") }
                        .forEach { file -> patchPluginsBlock(file, pluginDeclarations) }
                }
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
    val pluginVersion: String,
    val catalogName: String
) : Serializable {
    val declarationByAlias = "alias($catalogName.plugins.$pluginAlias)"
    val declarationById = "id(\"${pluginId}\")"
    val pluginMarkerWithoutVersion = "${pluginId}:${pluginId}.gradle.plugin"
}