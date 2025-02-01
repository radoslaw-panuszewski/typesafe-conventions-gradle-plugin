@file:Suppress("UnstableApiUsage")

package dev.panuszewski.gradle

import dev.panuszewski.gradle.util.createNewFile
import dev.panuszewski.gradle.util.readResourceAsString
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.dsl.VersionCatalogBuilder
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.catalog.LibrariesSourceGenerator
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.internal.management.VersionCatalogBuilderInternal
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.support.serviceOf
import java.io.StringWriter

internal class VersionCatalogAccessorsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            project.configure()
        }
    }

    private fun Project.configure() {
        getDeclaredVersionCatalogs().forEach { versionCatalog ->
            generateCatalogEntrypoint(versionCatalog)
            generateCatalogAccessors(versionCatalog)
        }
        registerSourceSet()
    }

    private fun Project.getDeclaredVersionCatalogs(): List<VersionCatalogBuilderInternal> =
        (gradle as GradleInternal)
            .settings
            .dependencyResolutionManagement
            .dependenciesModelBuilders
            .filterIsInstance<VersionCatalogBuilderInternal>()

    private fun Project.generateCatalogEntrypoint(catalog: VersionCatalogBuilderInternal) {
        val source = readResourceAsString("/Libs.kt")
            .replace("libs", catalog.name)
            .replace("Libs", catalog.capitalizedName)
        val file = createNewFile("$GENERATED_SOURCES_DIR/${catalog.capitalizedName}.kt")
        file.writeText(source)
    }

    private fun Project.generateCatalogAccessors(catalog: VersionCatalogBuilderInternal) {
        val source = generateCatalogAccessorsSource(catalog)
        val file = createNewFile("$GENERATED_SOURCES_DIR/org/gradle/accessors/dm/LibrariesFor${catalog.capitalizedName}.java")
        file.writeText(source)
    }

    private fun Project.generateCatalogAccessorsSource(catalog: VersionCatalogBuilderInternal): String {
        val model = catalog.build()
        val writer = StringWriter()
        LibrariesSourceGenerator.generateSource(writer, model, "org.gradle.accessors.dm", "LibrariesFor${catalog.capitalizedName}", serviceOf())
        return writer.toString()
    }

    private fun Project.registerSourceSet() {
        configure<SourceSetContainer> {
            named("main") {
                java.srcDir(GENERATED_SOURCES_DIR)
            }
        }
    }

    companion object {
        private const val GENERATED_SOURCES_DIR = "build/generated-sources/typesafe-conventions/kotlin"
    }
}

private val VersionCatalogBuilder.capitalizedName: String
    get() = name.capitalized()