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

        // when
        val scannedPaths = ConventionCatalogScanner.scanForConventionPlugins(rootDir)
            .map { it.relativeTo(rootDir.parentFile) }
            .map { it.path }

        // then
        scannedPaths shouldContainExactlyInAnyOrder listOf(
            "scanner-tests",
            "scanner-tests/subproject",
            "scanner-tests/subproject/src",
            "scanner-tests/subproject/src/foo.gradle.kts",
            "scanner-tests/subproject/src/main",
            "scanner-tests/subproject/src/main/kotlin",
            "scanner-tests/subproject/src/main/kotlin/foo.gradle.kts",
            "scanner-tests/subproject/src/customSourceSet",
            "scanner-tests/subproject/src/customSourceSet/foo.gradle.kts",
            "scanner-tests/src",
            "scanner-tests/src/foo.gradle.kts",
            "scanner-tests/src/main",
            "scanner-tests/src/main/kotlin",
            "scanner-tests/src/main/kotlin/foo.gradle.kts",
            "scanner-tests/src/customSourceSet",
            "scanner-tests/src/customSourceSet/foo.gradle.kts",
        )
    }
}
