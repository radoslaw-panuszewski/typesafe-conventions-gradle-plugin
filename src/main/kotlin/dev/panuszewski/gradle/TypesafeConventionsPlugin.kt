package dev.panuszewski.gradle

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.apply

internal class TypesafeConventionsPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        createSymlinkForGradleDir(settings)
        applyPluginToAllProjects(settings)
    }

    private fun createSymlinkForGradleDir(settings: Settings) {
        ProcessBuilder()
            .command("ln", "-s", "../gradle", "gradle")
            .directory(settings.rootDir)
            .start()
            .waitFor()
    }

    private fun applyPluginToAllProjects(settings: Settings) {
        settings.gradle.rootProject {
            allprojects {
                apply<VersionCatalogAccessorsPlugin>()
            }
        }
    }
}