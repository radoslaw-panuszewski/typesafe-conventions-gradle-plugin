@file:Suppress("UnstableApiUsage")

package dev.panuszewski.gradle

import dev.panuszewski.gradle.util.createNewFile
import dev.panuszewski.gradle.util.readResourceAsString
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.catalog.DefaultVersionCatalog
import org.gradle.api.internal.catalog.LibrariesSourceGenerator
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.internal.management.VersionCatalogBuilderInternal
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.support.serviceOf
import java.io.StringWriter

class TypesafeConventionsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            project.configure()
        }
    }

    private fun Project.configure() {
        generateCatalogEntrypoint()
        generateCatalogAccessors()
        registerSourceSet()
    }

    private fun Project.generateCatalogEntrypoint() {
        val source = readResourceAsString("/Libs.kt")
        val file = createNewFile("$GENERATED_SOURCES_DIR/Libs.kt")
        file.writeText(source)
    }

    private fun Project.generateCatalogAccessors() {
        val model = buildVersionCatalogModel()
        val source = generateCatalogAccessorsSource(model)
        val file = createNewFile("$GENERATED_SOURCES_DIR/org/gradle/accessors/dm/LibrariesForLibs.java")
        file.writeText(source)
    }

    private fun Project.buildVersionCatalogModel(): DefaultVersionCatalog {
        val versionCatalogBuilder = (gradle as GradleInternal).settings
            .dependencyResolutionManagement
            .dependenciesModelBuilders
            .get(0)
        return (versionCatalogBuilder as VersionCatalogBuilderInternal).build()
    }

    private fun Project.generateCatalogAccessorsSource(model: DefaultVersionCatalog): String {
        val writer = StringWriter()
        LibrariesSourceGenerator.generateSource(writer, model, "org.gradle.accessors.dm", "LibrariesForLibs", serviceOf())
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