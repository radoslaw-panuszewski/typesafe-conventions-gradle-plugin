package dev.panuszewski.gradle.parentbuild

import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.SettingsInternal
import org.gradle.api.model.ObjectFactory
import org.gradle.internal.management.VersionCatalogBuilderInternal
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

internal abstract class ParentBuild @Inject constructor(
    val gradle: GradleInternal,
    private val objects: ObjectFactory,
) {
    val settings: SettingsInternal get() = gradle.settings
    val versionCatalogs: List<ImportableVersionCatalog> by lazy { discoverVersionCatalogs() }

    private fun discoverVersionCatalogs(): List<ImportableVersionCatalog> {
        val builders = discoverBuilders()
        val tomlFiles = discoverTomlFiles()

        val versionCatalogsByName = buildMap {
            for (builder in builders) {
                computeIfAbsent(builder.name) { builder }
            }
            for (tomlFile in tomlFiles) {
                computeIfAbsent(tomlFile.name) { tomlFile }
            }
        }
        return versionCatalogsByName.values.toList()
    }

    private fun discoverBuilders(): List<ImportableVersionCatalog> =
        try {
            val catalogBuilders = settings
                .dependencyResolutionManagement
                .dependenciesModelBuilders
                .filterIsInstance<VersionCatalogBuilderInternal>()

            catalogBuilders.map { builder -> objects.newInstance<BuilderVersionCatalog>(builder) }
        } catch (_: IllegalStateException) {
            emptyList()
        }

    private fun discoverTomlFiles(): List<ImportableVersionCatalog> {
        val gradleDir = settings.rootDir.resolve("gradle")

        val tomlFiles = gradleDir
            .walk()
            .filter { it.name.endsWith(".versions.toml") }
            .toList()

        return tomlFiles.map { tomlFile -> objects.newInstance<TomlVersionCatalog>(tomlFile) }
    }
}
