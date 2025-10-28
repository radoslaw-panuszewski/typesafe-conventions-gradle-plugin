package dev.panuszewski.gradle.framework

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
     * Override, append or prepend content of `build.gradle.kts`
     */
    fun buildGradleKts(configurator: AppendableFile.() -> Any) {
        buildGradleKts.acceptConfigurator(configurator)
    }

    /**
     * Override, append or prepend content of `<subprojectName>/build.gradle.kts` and includes the subproject in the build
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
     * Override, append or prepend content of `settings.gradle.kts`
     */
    fun settingsGradleKts(configurator: AppendableFile.() -> Any) {
        settingsGradleKts.acceptConfigurator(configurator)
    }

    /**
     * Override, append or prepend content of `gradle/libs.versions.toml`
     */
    fun libsVersionsToml(configurator: AppendableFile.() -> Any) {
        customProjectFile("gradle/libs.versions.toml", configurator)
    }

    /**
     * Override, append or prepend content of a custom file under [path]
     */
    fun customProjectFile(path: String, configurator: AppendableFile.() -> Any) {
        customProjectFiles[path] = rootDir
            .resolveOrCreate(path)
            .let(::AppendableFile)
            .acceptConfigurator(configurator)
    }

    fun includeBuild(buildPath: String) {
        settingsGradleKts.append {
            """
            includeBuild("$buildPath")    
            """
        }
    }

    fun registerIncludedBuild(buildPath: String): GradleBuild {
        includeBuild(buildPath)
        val buildDir = rootDir.resolve(buildPath)
        val buildName = buildDir.name
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

    fun appendLine(content: () -> String) {
        append(content)
        append { "\n" }
    }

    fun prependLine(content: () -> String) {
        prepend(content)
        prepend { "\n" }
    }
}