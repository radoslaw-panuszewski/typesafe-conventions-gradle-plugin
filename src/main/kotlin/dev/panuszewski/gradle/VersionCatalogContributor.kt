package dev.panuszewski.gradle

import org.gradle.api.initialization.dsl.VersionCatalogBuilder
import org.gradle.api.initialization.resolve.MutableVersionCatalogContainer
import org.gradle.api.internal.file.FileOperations
import java.io.File
import javax.inject.Inject

internal interface VersionCatalogContributor {
    val catalogName: String
    fun contributeTo(container: MutableVersionCatalogContainer)
}

internal open class TomlFileVersionCatalogContributor @Inject constructor(
    private val tomlFile: File,
    private val fileOperations: FileOperations
) : VersionCatalogContributor {

    override val catalogName = tomlFile.name.substringBefore(".versions.toml")

    override fun contributeTo(container: MutableVersionCatalogContainer) {
        println("Contributing $catalogName from TOML file")

        container.create(catalogName) {
            from(fileOperations.configurableFiles(tomlFile))
        }
    }
}

internal open class BuilderVersionCatalogContributor @Inject constructor(
    private val builder: VersionCatalogBuilder
) : VersionCatalogContributor {

    override val catalogName = builder.name

    override fun contributeTo(container: MutableVersionCatalogContainer) {
        println("Contributing $catalogName from builder")
        container.add(builder)
    }
}