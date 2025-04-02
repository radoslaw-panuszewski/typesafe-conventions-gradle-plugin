package dev.panuszewski.gradle.util

import org.gradle.util.GradleVersion
import java.io.File

class GradleBuild(
    val rootProjectName: String,
    val rootDir: File,
    val gradleVersion: GradleVersion
) {
    private val buildGradleKts: GradleBuildscript
    private val settingsGradleKts: GradleBuildscript
    private val subprojectBuildGradleKts: MutableMap<String, GradleBuildscript> = mutableMapOf()

    init {
        rootDir.deleteRecursively()
        rootDir.mkdirs()
        buildGradleKts = GradleBuildscript(
            file = rootDir.resolveOrCreate("build.gradle.kts"),
            tailContent = """
                tasks {
                    withType<Wrapper> {
                        gradleVersion = "${gradleVersion.version}"
                    }
                }
                """
        )
        settingsGradleKts = GradleBuildscript(
            file = rootDir.resolveOrCreate("settings.gradle.kts"),
            tailContent = """
                rootProject.name = "$rootProjectName"
                
                buildCache {
                    local {
                        directory = "build-cache"
                    }
                }
                """
        )
    }

    /**
     * Set the full content of build.gradle.kts
     */
    fun buildGradleKts(configurator: GradleBuildscript.() -> Any) {
        buildGradleKts.acceptConfigurator(configurator)
    }

    /**
     * Set the full content of [subprojectName]/build.gradle.kts and includes the subproject into the build
     */
    fun subprojectBuildGradleKts(subprojectName: String, configurator: GradleBuildscript.() -> Any) {
        subprojectBuildGradleKts[subprojectName] = rootDir
            .resolveOrCreate("$subprojectName/build.gradle.kts")
            .let(::GradleBuildscript)
            .acceptConfigurator(configurator)

        settingsGradleKts.append {
            """
            include(":$subprojectName")    
            """
        }
    }

    /**
     * Set the full content of settings.gradle.kts
     */
    fun settingsGradleKts(configurator: GradleBuildscript.() -> Any) {
        settingsGradleKts.acceptConfigurator(configurator)
    }

    fun registerIncludedBuild(buildPath: String): GradleBuild {
        val buildDir = rootDir.resolve(buildPath)
        val buildName = buildDir.name

        settingsGradleKts.append {
            """
            includeBuild("$buildPath")    
            """
        }
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

class GradleBuildscript(
    private val file: File,
    private val tailContent: String? = null
) {
    init {
        appendTailContent()
    }

    fun acceptConfigurator(configurator: GradleBuildscript.() -> Any): GradleBuildscript {
        val maybeNewContent = configurator()

        if (maybeNewContent is String) {
            setContent { maybeNewContent }
        }
        return this
    }

    private fun setContent(content: () -> String) {
        file.writeText(content().trimIndent().trimStart())
        appendTailContent()
    }

    private fun appendTailContent() {
        tailContent?.let { append { it } }
    }

    fun prepend(content: () -> String) {
        val previousContent = file.readText()
        val separator = if (previousContent.isNotBlank()) "\n\n" else ""
        file.writeText(content().trimIndent().trimStart() + separator + previousContent)
    }

    fun append(content: () -> String) {
        val previousContent = file.readText()
        val separator = if (previousContent.isNotBlank()) "\n\n" else ""
        file.writeText(previousContent + separator + content().trimIndent().trimStart())
    }
}