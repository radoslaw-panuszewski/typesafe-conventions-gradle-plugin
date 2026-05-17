package dev.panuszewski.gradle.conventioncatalogs

import java.io.File

internal object ConventionCatalogScanner {
    /**
     * It enters only the `src` dirs to avoid scanning through huge folders like `build` or `.gradle`.
     *
     * Placing `*.gradle.kts` scripts directly in project dir is not supported.
     *
     * Example convention plugins that will be discovered:
     * - build-logic/src/main/kotlin/some-convention.gradle.kts
     * - build-logic/src/customSourceSet/some-convention.gradle.kts
     * - build-logic/src/some-convention.gradle.kts
     * - build-logic/subproject/src/main/kotlin/some-convention.gradle.kts
     * - build-logic/subproject/src/customSourceSet/some-convention.gradle.kts
     * - build-logic/subproject/src/some-convention.gradle.kts
     */
    fun scanForConventionPlugins(rootDir: File): List<File> =
        rootDir.walk()
            .onEnter {
                val isRoot = it == rootDir
                val inSrc = it.relativeTo(rootDir).path.split(File.separator).contains("src")
                val hasSrc = it.list()?.contains("src") == true
                val shouldEnter = isRoot || inSrc || hasSrc
                shouldEnter
            }
            .toList()
}
