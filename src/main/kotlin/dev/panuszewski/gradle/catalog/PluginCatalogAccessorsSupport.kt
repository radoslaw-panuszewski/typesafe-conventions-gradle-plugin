package dev.panuszewski.gradle.catalog

import dev.panuszewski.gradle.catalog.CatalogAccessorsPlugin.Companion.GENERATED_SOURCES_DIR_RELATIVE
import dev.panuszewski.gradle.dependencyWithRichVersion
import dev.panuszewski.gradle.util.capitalized
import dev.panuszewski.gradle.util.typesafeConventions
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.internal.catalog.DefaultVersionCatalog
import org.gradle.internal.management.VersionCatalogBuilderInternal
import org.gradle.kotlin.dsl.register
import java.io.File
import java.io.Serializable
import kotlin.text.RegexOption.DOT_MATCHES_ALL

internal object PluginCatalogAccessorsSupport {

    private const val MAIN_KOTLIN_SRC_DIR = "src/main/kotlin"
    private val PLUGIN_DECLARATION_BY_ALIAS: Regex = """.*alias\(.+\.plugins\.(.+)\).*""".toRegex()

    fun apply(project: Project, catalog: VersionCatalogBuilderInternal) {
        val pluginDeclarations = collectPluginDeclarations(project, catalog)

        writeCatalogEntrypointBeforeCompilation(project, catalog)
        patchPluginsBlocksAfterExtraction(project, pluginDeclarations)

        if (project.typesafeConventions.autoPluginDependencies.get()) {
            addPluginMarkerDependencies(project, pluginDeclarations)
        }
    }

    private fun collectPluginDeclarations(project: Project, catalog: VersionCatalogBuilderInternal): List<PluginDeclaration> {
        val model = catalog.build()
        val srcDir = project.layout.projectDirectory.file(MAIN_KOTLIN_SRC_DIR).asFile

        return srcDir.walk()
            .filter { file -> file.name.endsWith(".gradle.kts") }
            .map(File::readText)
            .map(::removeComments)
            .flatMap(String::lines)
            .mapNotNull { line -> parsePluginDeclaration(line, model) }
            .toList()
    }

    private fun removeComments(sourceCode: String): String =
        sourceCode
            .replace("""//.*""".toRegex(), "")
            .replace("""/\*.*?\*/""".toRegex(DOT_MATCHES_ALL), "")

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
                    pluginVersion = pluginModel.version,
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

    private fun patchPluginsBlocksAfterExtraction(project: Project, pluginDeclarations: List<PluginDeclaration>) {
        project.plugins.withId("org.gradle.kotlin.kotlin-dsl") {
            // we add action to an existing task instead of registering a dedicated task to allow caching
            // (otherwise the dedicated task would modify its own input and never be UP-TO-DATE)
            project.tasks.findByName("extractPrecompiledScriptPluginPlugins")?.apply {
                // this input is only needed to invalidate this task on changes in libs.versions.toml
                inputs.property("pluginDeclarations", pluginDeclarations)

                doLast {
                    outputs.files.asFileTree
                        .filter { file -> file.name.endsWith(".gradle.kts") }
                        .forEach { file -> patchPluginsBlock(file, pluginDeclarations) }
                }
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
            val pluginMarkerDependency = project.dependencyWithRichVersion(
                group = it.pluginMarkerGroup,
                name = it.pluginMarkerName,
                versionConstraint = it.pluginVersion
            )
            project.dependencies.add("implementation", pluginMarkerDependency)
        }
    }
}

private data class PluginDeclaration(
    val pluginAlias: String,
    val pluginId: String,
    val pluginVersion: VersionConstraint,
    val catalogName: String
) : Serializable {
    val declarationByAlias = "alias($catalogName.plugins.$pluginAlias)"
    val declarationById = "id(\"${pluginId}\")"
    val pluginMarkerGroup = pluginId
    val pluginMarkerName = "${pluginId}.gradle.plugin"
}