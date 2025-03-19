@file:Suppress("UnstableApiUsage")

package dev.panuszewski.gradle.catalog

import dev.panuszewski.gradle.catalog.CatalogAccessorsPlugin.Companion.GENERATED_SOURCES_DIR
import dev.panuszewski.gradle.catalog.CatalogAccessorsPlugin.Companion.GENERATED_SOURCES_DIR_RELATIVE
import dev.panuszewski.gradle.util.capitalized
import dev.panuszewski.gradle.util.createNewFile
import org.gradle.api.Project
import org.gradle.api.internal.catalog.LibrariesSourceGenerator
import org.gradle.api.problems.Problems
import org.gradle.internal.management.VersionCatalogBuilderInternal
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.serviceOf
import java.io.StringWriter

internal object LibraryCatalogAccessorsSupport {

    fun apply(project: Project, catalog: VersionCatalogBuilderInternal) {
        writeCatalogEntrypointBeforeCompilation(project, catalog)
        writeCatalogAccessorsBeforeCompilation(project, catalog)
    }

    private fun writeCatalogEntrypointBeforeCompilation(project: Project, catalog: VersionCatalogBuilderInternal) {
        val entrypointName = "EntrypointFor${catalog.name.capitalized}"

        val generateEntrypointTask = project.tasks.register<GenerateCatalogEntrypointTask>("generate$entrypointName") {
            this.catalogName.set(catalog.name)
            this.entrypointTemplateName.set("EntrypointForLibs")
            this.outputFile.set(project.layout.buildDirectory.file("$GENERATED_SOURCES_DIR_RELATIVE/$entrypointName.kt"))
        }

        project.tasks.named("compileKotlin") {
            dependsOn(generateEntrypointTask)
        }
    }

    private fun writeCatalogAccessorsBeforeCompilation(project: Project, catalog: VersionCatalogBuilderInternal) {
        val accessorsName = "LibrariesFor${catalog.name.capitalized}"

        // this task will never be UP-TO-DATE as otherwise it would not take into account changes in version catalog
        val generateAccessorsTask = project.tasks.register("generate$accessorsName") {
            doLast {
                val source = generateCatalogAccessorsSource(project, catalog)
                val file = project.createNewFile("$GENERATED_SOURCES_DIR/org/gradle/accessors/dm/$accessorsName.java")
                file.writeText(source)
            }
        }
        project.tasks.named("compileKotlin") {
            dependsOn(generateAccessorsTask)
        }
    }

    private fun generateCatalogAccessorsSource(project: Project, catalog: VersionCatalogBuilderInternal): String {
        val writer = StringWriter()
        val model = catalog.build()
        val packageName = "org.gradle.accessors.dm"
        val className = "LibrariesFor${catalog.name.capitalized}"
        val problemsService = project.serviceOf<Problems>()
        LibrariesSourceGenerator.generateSource(writer, model, packageName, className, problemsService)
        return writer.toString()
    }
}