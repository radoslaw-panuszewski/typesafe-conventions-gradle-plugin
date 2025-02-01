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
        try {
            createSymlinkForGradleDir(settings)
        } catch (e: Exception) {
            useVersionCatalogFromMainBuild(settings)
        }
        applyPluginToAllProjects(settings)
    }

    /**
     * It is purely to provide IDE support (allow jumping to version catalog definition in TOML file)
     */
    private fun createSymlinkForGradleDir(settings: Settings) {
        if (!settings.rootDir.resolve("gradle").exists()) {
            // TODO support Windows
            settings.runCommand("ln", "-s", "../gradle", "gradle")
            settings.runCommand("git", "add", "gradle")

            logger.lifecycle(
                "Created symlink for 'gradle' directory inside included build '${settings.rootProject.name}'. " +
                    "Run any task to make your IDE aware of it (it will let you jump to version catalog definition " +
                    "in *.versions.toml file). For example, you can just execute './gradlew help'"
            )
        }
    }

    /**
     * In case we can't create the symlink, we fall back to creating the version catalog programmatically
     */
    private fun useVersionCatalogFromMainBuild(settings: Settings) {
        settings.dependencyResolutionManagement.versionCatalogs {
            create("libs") {
                from(settings.serviceOf<FileOperations>().configurableFiles("gradle/libs.versions.toml"))
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

private fun Settings.runCommand(vararg command: String) {
    ProcessBuilder()
        .command(*command)
        .directory(rootDir)
        .start()
        .waitFor()
}