package dev.panuszewski.gradle.conventioncatalogs

import java.io.File

internal object ConventionCatalogScanner {
    /**
     * It enters only the `src` dirs to avoid scanning through huge folders like `build` or `.gradle`.
     *
     * Placing `*.gradle.kts` scripts directly in project dir is not supported.
     *
     * Example convention plugins that will be discovered:
     * - src/main/kotlin/foo.gradle.kts
     * - src/custom/foo.gradle.kts
     * - src/foo.gradle.kts
     * - subproject/src/foo.gradle.kts
     * - a/b/c/src/foo.gradle.kts
     *
     * Example convention plugins that won't be discovered:
     * - foo.gradle.kts
     * - subproject/foo.gradle.kts
     * - random-dir/foo.gradle.kts
     * - build/foo.gradle.kts
     * - gradle/foo.gradle.kts
     */
    fun scanForConventionPlugins(rootDir: File, projectDirs: List<File>?): List<File> =
        rootDir.walk()
            .onEnter { dir ->
                val isProjectDirPrefix = projectDirs?.any { projectDir -> projectDir.startsWith(dir) } ?: true
                val isInsideSrc = dir.relativeTo(rootDir).path.split(File.separator).contains("src")
                isProjectDirPrefix || isInsideSrc
            }
            .toList()
}
