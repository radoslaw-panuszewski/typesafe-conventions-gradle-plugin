@file:Suppress("UnstableApiUsage")

package dev.panuszewski.gradle.catalog

import dev.panuszewski.gradle.util.typesafeConventions
import dev.panuszewski.gradle.util.settings
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.internal.management.VersionCatalogBuilderInternal
import org.gradle.kotlin.dsl.configure

internal class CatalogAccessorsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            registerGeneratedSourceSet(project)

            val versionCatalogs = declaredVersionCatalogs(project)

            for (catalog in versionCatalogs) {
                LibraryCatalogAccessorsSupport.apply(project, catalog)

                if (project.typesafeConventions.accessorsInPluginsBlock.get()) {
                    PluginCatalogAccessorsSupport.apply(project, catalog)
                }
            }
        }
    }

    private fun registerGeneratedSourceSet(project: Project) {
        project.configure<SourceSetContainer> {
            named("main") {
                java.srcDir(GENERATED_SOURCES_DIR)
            }
        }
    }

    private fun declaredVersionCatalogs(project: Project): List<VersionCatalogBuilderInternal> =
        project.settings
            .dependencyResolutionManagement
            .dependenciesModelBuilders
            .filterIsInstance<VersionCatalogBuilderInternal>()

    companion object {
        // TODO separated source sets for generated Java and Kotlin files
        internal const val GENERATED_SOURCES_DIR = "build/generated-sources/typesafe-conventions/kotlin"
    }
}