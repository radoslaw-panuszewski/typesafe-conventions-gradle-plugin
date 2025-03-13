package dev.panuszewski.gradle.catalog

import dev.panuszewski.gradle.catalog.CatalogAccessorsPlugin.Companion.GENERATED_SOURCES_DIR
import dev.panuszewski.gradle.util.capitalizedName
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
    private val PLUGIN_DECLARATION_BY_ALIAS: Regex = """.*alias\(.+\.plugins\.(.+)\).*""".toRegex()

    fun apply(project: Project, catalog: VersionCatalogBuilderInternal) {
        val pluginDeclarations = collectPluginDeclarations(project, catalog)

        writeCatalogEntrypointBeforeCompilation(project, catalog)
        patchPluginsBlocksBeforeCompilation(project, catalog, pluginDeclarations)

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
        val entrypointName = "EntrypointFor${catalog.capitalizedName}InPluginsBlock"

        val generateEntrypointTask = project.tasks.register("generate$entrypointName") {
            outputs.file("$GENERATED_SOURCES_DIR/$entrypointName.kt")
            outputs.cacheIf { true }

            doLast {
                val source = readResourceAsString("/EntrypointForLibsInPluginsBlock.kt")
                    .replace("libs", catalog.name)
                    .replace("Libs", catalog.capitalizedName)

                outputs.files.singleFile.writeText(source)
            }
        }
        project.tasks.named("compileKotlin") {
            dependsOn(generateEntrypointTask)
        }
    }

    /**
     * The patchPluginsBlocksFor* task will never be UP-TO-DATE as it modifies output of another task.
     *
     * Theoretically, we could output the patched blocks to some other directory and set it as input for
     * compilePluginsBlocks task, but we would also need to add PluginSpecBuilders.kt to the input files
     * (see DefaultPrecompiledScriptPluginsSupport and GenerateExternalPluginSpecBuilders).
     *
     * There are 2 problems with the current implementation:
     * - it will be never UT-TO-DATE or FROM-CACHE (but there is still problem of invalidation when catalog changes)
     * - it requires 2 runs before configuration cache can be reused (probably not a big deal, but still)
     */
    private fun patchPluginsBlocksBeforeCompilation(
        project: Project,
        catalog: VersionCatalogBuilderInternal,
        pluginDeclarations: List<PluginDeclaration>
    ) {
        project.plugins.withId("org.gradle.kotlin.kotlin-dsl") {
            val taskName = "patchPluginsBlocksFor${catalog.capitalizedName}"

            val patchPluginsBlocksTask = project.tasks.register(taskName) {
                doLast {
                    val extractedPluginsBlocksDir =
                        project.layout.buildDirectory.file(EXTRACTED_PLUGINS_BLOCKS_DIR).get().asFile

                    extractedPluginsBlocksDir.walk()
                        .filter { file -> file.name.endsWith(".gradle.kts") }
                        .forEach { file -> patchPluginsBlock(file, pluginDeclarations) }
                }
            }
            project.tasks.findByName("compilePluginsBlocks")?.dependsOn(patchPluginsBlocksTask)
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
) {
    val declarationByAlias = "alias($catalogName.plugins.$pluginAlias)"
    val declarationById = "id(\"${pluginId}\")"
    val pluginMarkerWithoutVersion = "${pluginId}:${pluginId}.gradle.plugin"
}