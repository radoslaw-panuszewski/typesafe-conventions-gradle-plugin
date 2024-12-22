package dev.panuszewski.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.catalog.LibrariesSourceGenerator
import org.gradle.api.internal.catalog.parser.TomlCatalogFileParser
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.plugins.catalog.CatalogPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.internal.management.VersionCatalogBuilderInternal
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.support.serviceOf
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer
import java.io.StringWriter
import java.nio.file.Paths

class TypesafeConventionsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.configure()
    }

    private fun Project.configure() {
        apply(plugin = "version-catalog")

        var versionCatalogBuilder: VersionCatalogBuilderInternal? = null
        configure<CatalogPluginExtension> {
            versionCatalog {
                versionCatalogBuilder = this as VersionCatalogBuilderInternal
            }
        }
        TomlCatalogFileParser.parse(rootProject.file("../gradle/libs.versions.toml").toPath(), versionCatalogBuilder, serviceOf())
        val model = versionCatalogBuilder?.build()!!

        val file = file("build/generated/typesafe/main/java/org/gradle/accessors/dm/LibrariesForLibs.java")
        file.parentFile.mkdirs()
        file.createNewFile()
        val writer = StringWriter()
        LibrariesSourceGenerator.generateSource(writer, model, "org.gradle.accessors.dm", "LibrariesForLibs", serviceOf())
        file.writeText(writer.toString())

        val libsFile = file("build/generated/typesafe/main/kotlin/Libs.kt")
        libsFile.parentFile.mkdirs()
        libsFile.createNewFile()
        libsFile.writeText("""
            import org.gradle.accessors.dm.LibrariesForLibs
            import org.gradle.api.Project
            import org.gradle.api.plugins.catalog.CatalogPluginExtension
            import org.gradle.api.internal.catalog.parser.TomlCatalogFileParser
            import org.gradle.api.internal.project.ProjectInternal
            import org.gradle.internal.extensions.core.get
            import org.gradle.internal.management.VersionCatalogBuilderInternal
            import org.gradle.kotlin.dsl.configure
            import org.gradle.kotlin.dsl.newInstance

            internal val Project.libs: LibrariesForLibs
                get() {
                    var versionCatalogBuilder: VersionCatalogBuilderInternal? = null

                    if (!plugins.hasPlugin("version-catalog")) {
                        apply(mapOf("plugin" to "version-catalog"))

                        configure<CatalogPluginExtension> {
                            versionCatalog {
                                versionCatalogBuilder = this as VersionCatalogBuilderInternal
                            }
                        }
                        TomlCatalogFileParser.parse(rootProject.file("gradle/libs.versions.toml").toPath(), versionCatalogBuilder, (project as ProjectInternal).services.get())
                    } else {
                        configure<CatalogPluginExtension> {
                            versionCatalog {
                                versionCatalogBuilder = this as VersionCatalogBuilderInternal
                            }
                        }
                    }
                    val model = versionCatalogBuilder?.build()!!
                    return objects.newInstance(LibrariesForLibs::class, model)
                }
        """.trimIndent())

        configure<SourceSetContainer> {
            named("main") {
                java.srcDir("build/generated/typesafe/main/java")
            }
        }

        configure<KotlinSourceSetContainer> {
            sourceSets.named("main") {
                kotlin.srcDir("build/generated/typesafe/main/kotlin")
            }
        }
    }
}