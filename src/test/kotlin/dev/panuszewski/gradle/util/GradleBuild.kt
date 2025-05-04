package dev.panuszewski.gradle.util

import org.gradle.util.GradleVersion
import java.io.File

class GradleBuild(
    val rootProjectName: String,
    val rootDir: File,
    val gradleVersion: GradleVersion
) {
    private val buildGradleKts: AppendableFile
    private val settingsGradleKts: AppendableFile
    private val subprojectBuildGradleKts: MutableMap<String, AppendableFile> = mutableMapOf()
    private val customProjectFiles: MutableMap<String, AppendableFile> = mutableMapOf()

    init {
        rootDir.deleteRecursively()
        rootDir.mkdirs()
        buildGradleKts = AppendableFile(
            file = rootDir.resolveOrCreate("build.gradle.kts"),
            tailContent = """
                tasks {
                    withType<Wrapper> {
                        gradleVersion = "${gradleVersion.version}"
                    }
                }
                """
        )
        settingsGradleKts = AppendableFile(
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
    fun buildGradleKts(configurator: AppendableFile.() -> Any) {
        buildGradleKts.acceptConfigurator(configurator)
    }

    /**
     * Set the full content of [subprojectName]/build.gradle.kts and includes the subproject into the build
     */
    fun subprojectBuildGradleKts(subprojectName: String, configurator: AppendableFile.() -> Any) {
        subprojectBuildGradleKts[subprojectName] = rootDir
            .resolveOrCreate("$subprojectName/build.gradle.kts")
            .let(::AppendableFile)
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
    fun settingsGradleKts(configurator: AppendableFile.() -> Any) {
        settingsGradleKts.acceptConfigurator(configurator)
    }

    fun libsVersionsToml(configurator: AppendableFile.() -> Any) {
        customProjectFile("gradle/libs.versions.toml", configurator)
    }

    /**
     * Create the file under given [path] (relative to the test project root) with the given [content]
     */
    fun customProjectFile(path: String, configurator: AppendableFile.() -> Any) {
        subprojectBuildGradleKts[path] = rootDir
            .resolveOrCreate(path)
            .let(::AppendableFile)
            .acceptConfigurator(configurator)
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
}

class AppendableFile(
    private val file: File,
    private val tailContent: String? = null
) {
    init {
        appendTailContent()
    }

    fun acceptConfigurator(configurator: AppendableFile.() -> Any): AppendableFile {
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