package dev.panuszewski.gradle.catalog

import dev.panuszewski.gradle.catalog.CatalogAccessorsPlugin.Companion.GENERATED_SOURCES_DIR
import dev.panuszewski.gradle.util.capitalizedName
import dev.panuszewski.gradle.util.readResourceAsString
import dev.panuszewski.gradle.util.typesafeConventions
import org.gradle.api.Project
import org.gradle.api.internal.catalog.DefaultVersionCatalog
import org.gradle.internal.management.VersionCatalogBuilderInternal
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

internal object PluginCatalogAccessorsSupport {

    private const val MAIN_KOTLIN_SRC_DIR = "src/main/kotlin"
    private const val EXTRACTED_PLUGINS_BLOCKS_DIR = "kotlin-dsl/plugins-blocks/extracted"
    private val PLUGIN_DECLARATION_BY_ALIAS: Regex = """.*alias\(libs\.plugins\.(.+)\).*""".toRegex()

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
        project.tasks.withType<KotlinCompile>().configureEach {
            dependsOn(generateEntrypointTask)
        }
    }

    private fun patchPluginsBlocksBeforeCompilation(project: Project, pluginDeclarations: List<PluginDeclaration>) {
        project.plugins.withId("org.gradle.kotlin.kotlin-dsl") {
            /**
             * This task will never be UP-TO-DATE as it modifies output of another task.
             *
             * Theoretically, we could output the patched blocks to some other directory and set it as input for
             * compilePluginsBlocks task, but we would also need to add PluginSpecBuilders.kt to the input files
             * (see DefaultPrecompiledScriptPluginsSupport and GenerateExternalPluginSpecBuilders).
             *
             * There are 2 problems with the current implementation:
             * - it will be never UT-TO-DATE or FROM-CACHE (but there is still problem of invalidation when catalog changes)
             * - it requires 2 runs before configuration cache can be reused (probably not a big deal, but still)
             */
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