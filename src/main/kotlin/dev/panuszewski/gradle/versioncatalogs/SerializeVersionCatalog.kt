package dev.panuszewski.gradle.versioncatalogs

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.catalog.DefaultVersionCatalog
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.ObjectOutputStream

@CacheableTask
internal abstract class SerializeVersionCatalog : DefaultTask() {

    @Input
    val catalogModel: Property<DefaultVersionCatalog> = project.objects.property()

    @OutputFile
    val serializedCatalogModel: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    fun execute() {
        serializedCatalogModel.get().asFile.outputStream()
            .let(::ObjectOutputStream)
            .use { objectOutputStream -> objectOutputStream.writeObject(catalogModel.get()) }
    }
}
