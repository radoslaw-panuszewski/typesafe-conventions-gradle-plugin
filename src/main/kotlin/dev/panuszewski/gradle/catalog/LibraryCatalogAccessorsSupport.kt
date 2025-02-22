@file:Suppress("UnstableApiUsage")

package dev.panuszewski.gradle.catalog

import dev.panuszewski.gradle.catalog.CatalogAccessorsPlugin.Companion.GENERATED_SOURCES_DIR
import dev.panuszewski.gradle.util.capitalizedName
import dev.panuszewski.gradle.util.createNewFile
import dev.panuszewski.gradle.util.readResourceAsString
import org.gradle.api.Project
import org.gradle.api.internal.catalog.LibrariesSourceGenerator
import org.gradle.api.problems.Problems
import org.gradle.internal.management.VersionCatalogBuilderInternal
import org.gradle.kotlin.dsl.support.serviceOf
import java.io.StringWriter

internal object LibraryCatalogAccessorsSupport {

    fun apply(project: Project, catalog: VersionCatalogBuilderInternal) {
        writeCatalogEntrypoint(project, catalog)
        writeCatalogAccessors(project, catalog)
    }

    private fun writeCatalogEntrypoint(project: Project, catalog: VersionCatalogBuilderInternal) {
        val source = readResourceAsString("/EntrypointForLibs.kt")
            .replace("libs", catalog.name)
            .replace("Libs", catalog.capitalizedName)

        val file = project.createNewFile("$GENERATED_SOURCES_DIR/EntrypointFor${catalog.capitalizedName}.kt")

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
}