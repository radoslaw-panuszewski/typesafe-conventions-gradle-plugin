package dev.panuszewski.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.catalog.LibrariesSourceGenerator
import org.gradle.api.internal.catalog.parser.TomlCatalogFileParser
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.internal.management.VersionCatalogBuilderInternal
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.support.serviceOf
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer
import java.io.StringWriter

class TypesafeConventionsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.configure()
    }

    private fun Project.configure() {
        val versionCatalogs = (gradle as GradleInternal).settings
            .dependencyResolutionManagement
            .versionCatalogs

        val versionCatalogName = "typesafeConventionsInternalCatalog"

        val versionCatalogBuilder = versionCatalogs.findByName(versionCatalogName)
            ?: run {
                val builder = versionCatalogs.create(versionCatalogName)
                TomlCatalogFileParser.parse(rootProject.file("../gradle/libs.versions.toml").toPath(), builder, serviceOf())
                builder
            }

        val model = (versionCatalogBuilder as VersionCatalogBuilderInternal).build()

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
            import org.gradle.api.internal.GradleInternal
            import org.gradle.api.internal.catalog.parser.TomlCatalogFileParser
            import org.gradle.internal.management.VersionCatalogBuilderInternal
            import org.gradle.kotlin.dsl.newInstance
            import org.gradle.kotlin.dsl.support.serviceOf
            
            internal val Project.libs: LibrariesForLibs
                get() {
                    val versionCatalogs = (gradle as GradleInternal).settings
                        .dependencyResolutionManagement
                        .versionCatalogs
                    
                    val versionCatalogName = "typesafeConventionsInternalCatalog"
                    
                    val versionCatalogBuilder = versionCatalogs.findByName(versionCatalogName)
                        ?: run {
                            val builder = versionCatalogs.create(versionCatalogName)
                            TomlCatalogFileParser.parse(rootProject.file("gradle/libs.versions.toml").toPath(), builder, serviceOf())
                            builder
                        }
            
                    val model = (versionCatalogBuilder as VersionCatalogBuilderInternal).build()
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