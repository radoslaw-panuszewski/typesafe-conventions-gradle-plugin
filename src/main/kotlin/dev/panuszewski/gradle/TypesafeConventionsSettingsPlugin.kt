package dev.panuszewski.gradle

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.file.FileOperations
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.support.serviceOf

class TypesafeConventionsSettingsPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        settings.dependencyResolutionManagement {
            versionCatalogs {
                create("libs") {
                    from(settings.serviceOf<FileOperations>().configurableFiles("gradle/libs.versions.toml"))
                }
            }
        }

        settings.gradle.rootProject {
            allprojects {
                afterEvaluate {
                    apply(plugin = "dev.panuszewski.typesafe-conventions")
                }
            }
        }
    }
}