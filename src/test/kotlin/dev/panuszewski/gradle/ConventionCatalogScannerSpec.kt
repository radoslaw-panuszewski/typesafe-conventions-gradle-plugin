package dev.panuszewski.gradle

import dev.panuszewski.gradle.conventioncatalogs.ConventionCatalogScanner
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.api.Test
import java.io.File

class ConventionCatalogScannerSpec {

    @Test
    fun `should only scan src directories`() {
        // given
        val rootDir = File("src/test/resources/scanner-tests")
        val projectDirs = listOf(rootDir, rootDir.resolve("subproject"), rootDir.resolve("a/b/c"))

        // when
        val scannedPaths = ConventionCatalogScanner.scanForConventionPlugins(rootDir, projectDirs)
            .map { it.relativeTo(rootDir) }
            .map { it.path }

        // then
        scannedPaths shouldContainExactlyInAnyOrder listOf(
            "",
            "src",
            "src/foo.gradle.kts",
            "src/main",
            "src/main/kotlin",
            "src/main/kotlin/foo.gradle.kts",
            "src/customSourceSet",
            "src/customSourceSet/foo.gradle.kts",
            "subproject",
            "subproject/src",
            "subproject/src/foo.gradle.kts",
            "subproject/src/main",
            "subproject/src/main/kotlin",
            "subproject/src/main/kotlin/foo.gradle.kts",
            "subproject/src/customSourceSet",
            "subproject/src/customSourceSet/foo.gradle.kts",
            "a",
            "a/b",
            "a/b/c",
            "a/b/c/src",
            "a/b/c/src/foo.gradle.kts",
        )
    }
}
