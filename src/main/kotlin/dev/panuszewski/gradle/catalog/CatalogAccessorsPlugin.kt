@file:Suppress("UnstableApiUsage")

package dev.panuszewski.gradle.catalog

import dev.panuszewski.gradle.KOTLIN_GRADLE_PLUGIN_ID
import dev.panuszewski.gradle.util.settings
import dev.panuszewski.gradle.util.typesafeConventions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.internal.management.VersionCatalogBuilderInternal
import org.gradle.kotlin.dsl.configure

internal class CatalogAccessorsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.withId(KOTLIN_GRADLE_PLUGIN_ID) {
            registerGeneratedSourceSet(project)

            val versionCatalogs = declaredVersionCatalogs(project)

            if (versionCatalogs.isEmpty()) {
                logger.warn("No version catalogs found in project ${project.name}. The typesafe accessors won't be generated.")
            } else {
                generateAccessors(project, versionCatalogs)
            }
        }
    }

    private fun registerGeneratedSourceSet(project: Project) {
        project.configure<SourceSetContainer> {
            named("main") {
                java.srcDir(project.layout.buildDirectory.dir(GENERATED_SOURCES_DIR_RELATIVE))
            }
        }
    }

    private fun declaredVersionCatalogs(project: Project): List<VersionCatalogBuilderInternal> =
        project.settings
            .dependencyResolutionManagement
            .dependenciesModelBuilders
            .filterIsInstance<VersionCatalogBuilderInternal>()

    @Suppress("DEPRECATION")
    private fun generateAccessors(project: Project, versionCatalogs: List<VersionCatalogBuilderInternal>) {
        for (catalog in versionCatalogs) {
            LibraryCatalogAccessorsSupport.apply(project, catalog)

            if (project.typesafeConventions.accessorsInPluginsBlock.get()) {
                PluginCatalogAccessorsSupport.apply(project, catalog)
            }
        }
    }

    companion object {
        private val logger = Logging.getLogger(CatalogAccessorsPlugin::class.java)
        // TODO separated source sets for generated Java and Kotlin files
        internal const val GENERATED_SOURCES_DIR_RELATIVE = "generated-sources/typesafe-conventions/kotlin"
    }
}