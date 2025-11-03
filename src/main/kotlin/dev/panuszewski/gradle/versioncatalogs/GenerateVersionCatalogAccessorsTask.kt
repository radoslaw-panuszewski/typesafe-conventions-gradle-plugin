@file:Suppress("UnstableApiUsage")

package dev.panuszewski.gradle.versioncatalogs

import dev.panuszewski.gradle.util.capitalized
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.catalog.DefaultVersionCatalog
import org.gradle.api.internal.catalog.LibrariesSourceGenerator
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.StringWriter
import javax.inject.Inject

@CacheableTask
internal abstract class GenerateVersionCatalogAccessorsTask : DefaultTask() {

    @Input
    val catalogModel: Property<DefaultVersionCatalog> = project.objects.property()

    @OutputFile
    val outputFile: RegularFileProperty = project.objects.fileProperty()

    @get:Inject
    abstract val problems: Problems

    @TaskAction
    fun execute() {
        val source = generateCatalogAccessorsSource()
        outputFile.get().asFile.writeText(source)
    }

    private fun generateCatalogAccessorsSource(): String {
        val writer = StringWriter()
        val packageName = "org.gradle.accessors.dm"
        val className = "LibrariesFor${catalogModel.get().name.capitalized}"
        LibrariesSourceGenerator.generateSource(writer, catalogModel.get(), packageName, className, problems)
        return writer.toString()
    }
}