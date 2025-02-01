package dev.panuszewski.gradle.util

import org.gradle.util.GradleVersion
import java.io.File

class GradleBuild(
    val rootProjectName: String,
    val rootDir: File,
    val gradleVersion: GradleVersion
) {
    val buildscript: File
    val settings: File
    val subprojectBuildscripts: MutableMap<String, File> = mutableMapOf()

    init {
        rootDir.deleteRecursively()
        rootDir.mkdirs()
        buildscript = rootDir.resolveOrCreate("build.gradle.kts")
        settings = rootDir.resolveOrCreate("settings.gradle.kts")
        settings.writeText(
            """
            rootProject.name = "$rootProjectName"
            """.trimIndent()
        )
    }

    /**
     * Set the full content of build.gradle.kts
     */
    fun buildGradleKts(content: () -> String) {
        buildscript.writeText(content().trimIndent().trimStart())
        buildscript.appendText(buildGradleKtsTailContent())
    }

    private fun buildGradleKtsTailContent() = buildString {
        append("\n\n")
        append(
            """
            tasks {
                withType<Wrapper> {
                    gradleVersion = "${gradleVersion.version}"
                }
            }
            """.trimIndent()
        )
    }

    /**
     * Set the full content of [subprojectName]/build.gradle.kts and includes the subproject into the build
     */
    fun subprojectBuildGradleKts(subprojectName: String, content: () -> String) {
        subprojectBuildscripts[subprojectName] = rootDir
            .resolveOrCreate("$subprojectName/build.gradle.kts")
            .apply { writeText(content().trimIndent().trimStart()) }

        settings.appendText("\n")
        settings.appendText(
            """
            include(":$subprojectName")    
            """.trimIndent()
        )
    }

    /**
     * Set the full content of settings.gradle.kts
     */
    fun settingsGradleKts(content: () -> String) {
        settings.writeText(content().trimIndent().trimStart())
    }

    fun registerIncludedBuild(buildPath: String): GradleBuild {
        val buildDir = rootDir.resolve(buildPath)
        val buildName = buildDir.name

        settings.appendText("\n")
        settings.appendText(
            """
            includeBuild("$buildPath")    
            """.trimIndent()
        )
        return GradleBuild(buildName, buildDir, gradleVersion)
    }

    /**
     * Create the file under given [path] (relative to the test project root) with the given [content]
     */
    fun customProjectFile(path: String, content: () -> String): File {
        val file = rootDir.resolveOrCreate(path)
        file.writeText(content().trimIndent())
        return file
    }
}