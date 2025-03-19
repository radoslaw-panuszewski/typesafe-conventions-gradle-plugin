@file:Suppress("UnstableApiUsage")

package dev.panuszewski.gradle.catalog

import dev.panuszewski.gradle.util.capitalized
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.catalog.DefaultVersionCatalog
import org.gradle.api.internal.catalog.LibrariesSourceGenerator
import org.gradle.api.model.ObjectFactory
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.StringWriter
import javax.inject.Inject

// TODO tests for caching and invalidating on input changes
@CacheableTask
internal abstract class GenerateCatalogAccessorsTask @Inject constructor(
    objects: ObjectFactory,
    private val problems: Problems
) : DefaultTask() {

    @Input
    val catalogModel: Property<DefaultVersionCatalog> = objects.property()

    @OutputFile
    val outputFile: RegularFileProperty = objects.fileProperty()

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