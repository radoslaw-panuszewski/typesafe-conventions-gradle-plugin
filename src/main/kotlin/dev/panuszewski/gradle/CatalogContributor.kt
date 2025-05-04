package dev.panuszewski.gradle

import org.gradle.api.initialization.dsl.VersionCatalogBuilder
import org.gradle.api.initialization.resolve.MutableVersionCatalogContainer
import org.gradle.api.model.ObjectFactory
import java.io.File
import javax.inject.Inject

internal interface CatalogContributor {
    val catalogName: String
    fun contributeTo(container: MutableVersionCatalogContainer)
}

internal open class TomlCatalogContributor @Inject constructor(
    private val tomlFile: File,
    private val objects: ObjectFactory
) : CatalogContributor {

    override val catalogName = tomlFile.name.substringBefore(".versions.toml")

    override fun contributeTo(container: MutableVersionCatalogContainer) {
        container.create(catalogName) {
            from(objects.fileCollection().from(tomlFile))
        }
    }
}

internal open class BuilderCatalogContributor @Inject constructor(
    private val builder: VersionCatalogBuilder
) : CatalogContributor {

    override val catalogName = builder.name

    override fun contributeTo(container: MutableVersionCatalogContainer) {
        container.add(builder)
    }
}