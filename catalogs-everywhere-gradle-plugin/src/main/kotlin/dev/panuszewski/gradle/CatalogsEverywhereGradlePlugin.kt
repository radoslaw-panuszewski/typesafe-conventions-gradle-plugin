package dev.panuszewski.gradle

import org.gradle.api.Project
import org.gradle.api.internal.catalog.LibrariesSourceGenerator
import org.gradle.api.plugins.catalog.CatalogPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.internal.management.VersionCatalogBuilderInternal
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.support.serviceOf
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import java.io.StringWriter

class CatalogsEverywhereGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(project: Project) {
        project.apply(plugin = "org.gradle.version-catalog")
        project.configure()
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        return kotlinCompilation.target.project.provider { emptyList() }
    }

    override fun getCompilerPluginId(): String = "dev.panuszewski.catalogs-everywhere"

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(groupId = "dev.panuszewski", artifactId = "catalogs-everywhere-kotlin-plugin")

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>) = true

    private fun Project.configure() {
        var versionCatalogBuilder: VersionCatalogBuilderInternal? = null
        configure<CatalogPluginExtension> {
            versionCatalog {
                it.from(files("../gradle/libs.versions.toml"))
                versionCatalogBuilder = it as VersionCatalogBuilderInternal
            }
        }
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
            import org.gradle.internal.management.VersionCatalogBuilderInternal
            import org.gradle.kotlin.dsl.configure
            import org.gradle.kotlin.dsl.newInstance
            
            val Project.libs: LibrariesForLibs
                get() {
                    var versionCatalogBuilder: VersionCatalogBuilderInternal? = null
                    configure<CatalogPluginExtension> {
                        versionCatalog {
                            from(files("gradle/libs.versions.toml"))
                            versionCatalogBuilder = this as VersionCatalogBuilderInternal
                        }
                    }
                    val model = versionCatalogBuilder?.build()!!
            
                    return objects.newInstance(LibrariesForLibs::class, model)
                }
        """.trimIndent())

        configure<SourceSetContainer> {
            named("main") {
                it.java.srcDir("build/generated/typesafe/main/java")
            }
        }

        configure<KotlinSourceSetContainer> {
            sourceSets.named("main") {
                it.kotlin.srcDir("build/generated/typesafe/main/kotlin")
            }
        }
    }
}