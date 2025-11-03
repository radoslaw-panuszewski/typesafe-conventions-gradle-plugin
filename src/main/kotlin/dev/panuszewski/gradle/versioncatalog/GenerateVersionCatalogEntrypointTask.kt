package dev.panuszewski.gradle.versioncatalog

import dev.panuszewski.gradle.util.capitalized
import dev.panuszewski.gradle.util.readResourceAsString
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@CacheableTask
internal abstract class GenerateVersionCatalogEntrypointTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    @Input
    val catalogName: Property<String> = objects.property<String>()

    @Input
    val entrypointTemplateName: Property<String> = objects.property<String>()

    @OutputFile
    val outputFile: RegularFileProperty = objects.fileProperty()

    @TaskAction
    fun execute() {
        val source = readResourceAsString("/${entrypointTemplateName.get()}.kt")
            .replace("catalog", catalogName.get())
            .replace("Catalog", catalogName.get().capitalized)

        outputFile.get().asFile.writeText(source)
    }
}