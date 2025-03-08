package dev.panuszewski.gradle.util

import org.gradle.util.GradleVersion
import java.io.File

class GradleBuild(
    val rootProjectName: String,
    val rootDir: File,
    val gradleVersion: GradleVersion
) {
    private val buildGradleKts: WrapperConfiguringGradleBuildscript
    private val settingsGradleKts: GradleBuildscript
    private val subprojectBuildGradleKts: MutableMap<String, GradleBuildscript> = mutableMapOf()

    init {
        rootDir.deleteRecursively()
        rootDir.mkdirs()
        buildGradleKts = WrapperConfiguringGradleBuildscript(rootDir.resolveOrCreate("build.gradle.kts"), gradleVersion)
        settingsGradleKts = GradleBuildscript(rootDir.resolveOrCreate("settings.gradle.kts"))
        settingsGradleKts.setContent {
            """
            rootProject.name = "$rootProjectName"
            """
        }
    }

    /**
     * Set the full content of build.gradle.kts
     */
    fun buildGradleKts(configurator: GradleBuildscript.() -> String) {
        buildGradleKts.acceptConfigurator(configurator)
    }

    /**
     * Set the full content of [subprojectName]/build.gradle.kts and includes the subproject into the build
     */
    fun subprojectBuildGradleKts(subprojectName: String, configurator: GradleBuildscript.() -> String) {
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

open class GradleBuildscript(
    private val buildscriptFile: File
) {
    fun acceptConfigurator(configurator: GradleBuildscript.() -> Any): GradleBuildscript {
        val maybeNewContent = configurator()

        if (maybeNewContent is String) {
            setContent { maybeNewContent }
        }
        return this
    }

    open fun setContent(content: () -> String) {
        buildscriptFile.writeText(content().trimIndent().trimStart())
    }

    fun prepend(content: () -> String) {
        val previousSettingsContent = buildscriptFile.readText()
        buildscriptFile.writeText(content().trimIndent().trimStart() + "\n\n" + previousSettingsContent)
    }

    fun append(content: () -> String) {
        buildscriptFile.appendText("\n\n" + content().trimIndent().trimStart())
    }
}

class WrapperConfiguringGradleBuildscript(
    file: File,
    private val gradleVersion: GradleVersion
) : GradleBuildscript(file) {

    override fun setContent(content: () -> String) {
        super.setContent(content)
        append {
            """
            tasks {
                withType<Wrapper> {
                    gradleVersion = "${gradleVersion.version}"
                }
            }
            """
        }
    }
}