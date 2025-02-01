package dev.panuszewski.gradle

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.logging.Logging
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.support.serviceOf

@Suppress("unused") // used as plugin implementation class
internal class TypesafeConventionsPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        useVersionCatalogsFromMainBuild(settings)
        applyPluginToAllProjects(settings)
    }

    private fun useVersionCatalogsFromMainBuild(settings: Settings) {
        settings.dependencyResolutionManagement.versionCatalogs {
            settings.rootDir.resolve("../gradle")
                .walk()
                .map { it.name }
                .filter { it.endsWith(".versions.toml") }
                .forEach { tomlFileName ->
                    create(tomlFileName.substringBefore(".versions.toml")) {
                        from(settings.serviceOf<FileOperations>().configurableFiles("gradle/$tomlFileName"))
                    }
                }
        }
    }

    private fun applyPluginToAllProjects(settings: Settings) {
        settings.gradle.rootProject {
            allprojects {
                apply<VersionCatalogAccessorsPlugin>()
            }
        }
    }

    companion object {
        private val logger = Logging.getLogger(TypesafeConventionsPlugin::class.java)
    }
}