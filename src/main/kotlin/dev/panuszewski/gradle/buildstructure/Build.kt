package dev.panuszewski.gradle.buildstructure

import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.SettingsInternal
import org.gradle.api.model.ObjectFactory
import org.gradle.internal.management.VersionCatalogBuilderInternal
import org.gradle.kotlin.dsl.newInstance

internal class Build(
    val gradle: GradleInternal,
    val settings: SettingsInternal = gradle.settings,
    private val parentProvider: () -> Build?,
    private val objects: ObjectFactory,
) {
    val parent: Build? by lazy { parentProvider.invoke() }
    val versionCatalogs: List<VersionCatalog> by lazy { discoverVersionCatalogs() }

    private fun discoverVersionCatalogs(): List<VersionCatalog> {
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

    private fun discoverBuilders(): List<VersionCatalog> =
        try {
            val catalogBuilders = settings
                .dependencyResolutionManagement
                .dependenciesModelBuilders
                .filterIsInstance<VersionCatalogBuilderInternal>()

            catalogBuilders.map { builder -> objects.newInstance<BuilderVersionCatalog>(builder) }
        } catch (_: IllegalStateException) {
            emptyList()
        }

    private fun discoverTomlFiles(): List<VersionCatalog> {
        val gradleDir = settings.rootDir.resolve("gradle")

        val tomlFiles = gradleDir
            .walk()
            .filter { it.name.endsWith(".versions.toml") }
            .toList()

        return tomlFiles.map { tomlFile -> objects.newInstance<TomlVersionCatalog>(tomlFile) }
    }
}
