package dev.panuszewski.gradle.buildstructure

import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.dsl.VersionCatalogBuilder
import org.gradle.api.model.ObjectFactory
import java.io.File
import javax.inject.Inject

internal interface VersionCatalog {
    val name: String

    fun contributeTo(settings: Settings)
}

internal open class TomlVersionCatalog @Inject constructor(
    private val tomlFile: File,
    private val objects: ObjectFactory
) : VersionCatalog {

    override val name = tomlFile.name.substringBefore(".versions.toml")

    override fun contributeTo(settings: Settings) {
        settings.dependencyResolutionManagement.versionCatalogs.create(name) {
            from(objects.fileCollection().from(tomlFile))
        }
    }
}

internal open class BuilderVersionCatalog @Inject constructor(
    private val builder: VersionCatalogBuilder
) : VersionCatalog {

    override val name = builder.name

    override fun contributeTo(settings: Settings) {
        settings.dependencyResolutionManagement.versionCatalogs.add(builder)
    }
}