@file:Suppress("UnstableApiUsage")

package dev.panuszewski.gradle.versioncatalogs

import dev.panuszewski.gradle.util.capitalized
import dev.panuszewski.gradle.versioncatalogs.VersionCatalogAccessorsPlugin.Companion.GENERATED_SOURCES_DIR_RELATIVE
import org.gradle.api.Project
import org.gradle.internal.management.VersionCatalogBuilderInternal
import org.gradle.kotlin.dsl.register

internal fun configureLibraryVersionCatalogAccessors(project: Project, catalogs: List<VersionCatalogBuilderInternal>) {
    for (catalog in catalogs) {
        writeCatalogEntrypointBeforeCompilation(project, catalog)
        writeCatalogAccessorsBeforeCompilation(project, catalog)
    }
}

private fun writeCatalogEntrypointBeforeCompilation(project: Project, catalog: VersionCatalogBuilderInternal) {
    val entrypointName = "EntrypointFor${catalog.name.capitalized}"

    val generateEntrypointTask = project.tasks.register<GenerateVersionCatalogEntrypointTask>("generate$entrypointName") {
        this.catalogName.set(catalog.name)
        this.entrypointTemplateName.set("EntrypointForCatalog")
        this.outputFile.set(project.layout.buildDirectory.file("${GENERATED_SOURCES_DIR_RELATIVE}/$entrypointName.kt"))
    }

    project.tasks.named("compileKotlin") {
        dependsOn(generateEntrypointTask)
    }
}

private fun writeCatalogAccessorsBeforeCompilation(project: Project, catalog: VersionCatalogBuilderInternal) {
    val accessorsName = "LibrariesFor${catalog.name.capitalized}"

    val generateAccessorsTask = project.tasks.register<GenerateVersionCatalogAccessorsTask>("generate$accessorsName") {
        this.catalogModel.set(catalog.build())
        this.outputFile.set(project.layout.buildDirectory.file("${GENERATED_SOURCES_DIR_RELATIVE}/org/gradle/accessors/dm/$accessorsName.java"))
    }

    project.tasks.named("compileKotlin") {
        dependsOn(generateAccessorsTask)
    }
}
