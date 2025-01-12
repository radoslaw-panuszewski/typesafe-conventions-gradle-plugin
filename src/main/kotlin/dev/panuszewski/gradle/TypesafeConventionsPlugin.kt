@file:Suppress("UnstableApiUsage")

package dev.panuszewski.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.GradleInternal
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
        val versionCatalogBuilder = (gradle as GradleInternal).settings
            .dependencyResolutionManagement
            .dependenciesModelBuilders
            .get(0)

        val model = (versionCatalogBuilder as VersionCatalogBuilderInternal).build()

        val file = file("build/generated-sources/typesafe-conventions/kotlin/org/gradle/accessors/dm/LibrariesForLibs.java")
        file.parentFile.mkdirs()
        file.createNewFile()
        val writer = StringWriter()
        LibrariesSourceGenerator.generateSource(writer, model, "org.gradle.accessors.dm", "LibrariesForLibs", serviceOf())
        file.writeText(writer.toString())

        val libsFile = file("build/generated-sources/typesafe-conventions/kotlin/Libs.kt")
        libsFile.parentFile.mkdirs()
        libsFile.createNewFile()
        libsFile.writeText(TypesafeConventionsPlugin::class.java.getResourceAsStream("/Libs.kt").bufferedReader().readText())

        configure<SourceSetContainer> {
            named("main") {
                java.srcDir("build/generated-sources/typesafe-conventions/kotlin")
            }
        }
    }
}