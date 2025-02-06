@file:Suppress("UnstableApiUsage")

package dev.panuszewski.gradle

import dev.panuszewski.gradle.util.createNewFile
import dev.panuszewski.gradle.util.readResourceAsString
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.dsl.VersionCatalogBuilder
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.catalog.LibrariesSourceGenerator
import org.gradle.api.problems.Problems
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.internal.management.VersionCatalogBuilderInternal
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.support.serviceOf
import java.io.StringWriter

internal class VersionCatalogAccessorsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            registerGeneratedSourceSet(project)

            val versionCatalogs = declaredVersionCatalogs(project)

            for (catalog in versionCatalogs) {
                writeCatalogEntrypoint(project, catalog)
                writeCatalogAccessors(project, catalog)
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
        (project.gradle as GradleInternal)
            .settings
            .dependencyResolutionManagement
            .dependenciesModelBuilders
            .filterIsInstance<VersionCatalogBuilderInternal>()

    private fun writeCatalogEntrypoint(project: Project, catalog: VersionCatalogBuilderInternal) {
        val source = readResourceAsString("/Libs.kt")
            .replace("libs", catalog.name)
            .replace("Libs", catalog.capitalizedName)
        val file = project.createNewFile("$GENERATED_SOURCES_DIR/${catalog.capitalizedName}.kt")
        file.writeText(source)
    }

    private fun writeCatalogAccessors(project: Project, catalog: VersionCatalogBuilderInternal) {
        val source = generateCatalogAccessorsSource(project, catalog)
        val file = project.createNewFile("$GENERATED_SOURCES_DIR/org/gradle/accessors/dm/LibrariesFor${catalog.capitalizedName}.java")
        file.writeText(source)
    }

    private fun generateCatalogAccessorsSource(project: Project, catalog: VersionCatalogBuilderInternal): String {
        val writer = StringWriter()
        val model = catalog.build()
        val packageName = "org.gradle.accessors.dm"
        val className = "LibrariesFor${catalog.capitalizedName}"
        val problemsService = project.serviceOf<Problems>()
        LibrariesSourceGenerator.generateSource(writer, model, packageName, className, problemsService)
        return writer.toString()
    }

    companion object {
        private const val GENERATED_SOURCES_DIR = "build/generated-sources/typesafe-conventions/kotlin"
    }
}

private val VersionCatalogBuilder.capitalizedName: String
    get() = name.capitalized()