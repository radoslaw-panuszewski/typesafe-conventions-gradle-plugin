package dev.panuszewski.gradle

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.apply

class TypesafeConventionsSettingsPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        ProcessBuilder()
            .command("ln", "-s", "../gradle", "gradle")
            .directory(settings.rootDir)
            .start()
            .waitFor()

        settings.gradle.rootProject {
            allprojects {
                afterEvaluate {
                    apply(plugin = "dev.panuszewski.typesafe-conventions")
                }
            }
        }
    }
}