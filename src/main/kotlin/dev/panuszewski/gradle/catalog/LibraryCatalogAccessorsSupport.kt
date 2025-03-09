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
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.StringWriter

internal object LibraryCatalogAccessorsSupport {

    fun apply(project: Project, catalog: VersionCatalogBuilderInternal) {
        writeCatalogEntrypointBeforeCompilation(project, catalog)
        writeCatalogAccessorsBeforeCompilation(project, catalog)
    }

    private fun writeCatalogEntrypointBeforeCompilation(project: Project, catalog: VersionCatalogBuilderInternal) {
        val entrypointName = "EntrypointFor${catalog.capitalizedName}"

        val generateEntrypointTask = project.tasks.register("generate$entrypointName") {
            outputs.file("$GENERATED_SOURCES_DIR/$entrypointName.kt")
            outputs.cacheIf { true }

            doLast {
                val source = readResourceAsString("/EntrypointForLibs.kt")
                    .replace("libs", catalog.name)
                    .replace("Libs", catalog.capitalizedName)

                outputs.files.singleFile.writeText(source)
            }
        }
        project.tasks.withType<KotlinCompile>().configureEach {
            dependsOn(generateEntrypointTask)
        }
    }

    private fun writeCatalogAccessorsBeforeCompilation(project: Project, catalog: VersionCatalogBuilderInternal) {
        val accessorsName = "LibrariesFor${catalog.capitalizedName}"

        // this task will never be UP-TO-DATE as otherwise it would not take into account changes in version catalog
        val generateAccessorsTask = project.tasks.register("generate$accessorsName") {
            doLast {
                val source = generateCatalogAccessorsSource(project, catalog)
                val file = project.createNewFile("$GENERATED_SOURCES_DIR/org/gradle/accessors/dm/$accessorsName.java")
                file.writeText(source)
            }
        }
        project.tasks.withType<KotlinCompile>().configureEach {
            dependsOn(generateAccessorsTask)
        }
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