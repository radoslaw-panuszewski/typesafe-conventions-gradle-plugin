package dev.panuszewski.gradle.util

import dev.panuszewski.gradle.util.BuildOutcome.BUILD_FAILED
import dev.panuszewski.gradle.util.BuildOutcome.BUILD_SUCCESSFUL
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayNameGeneration
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import java.io.File
import java.io.StringWriter
import java.nio.file.Paths
import java.util.stream.Stream

/**
 * NOTE: Always execute the tests via Gradle!
 *
 * After executing the tests, every test project will be saved in `.test-projects/{test-name}`.
 * You can use it for debugging.
 *
 * Option 1: Open in IntelliJ:
 * ```bash
 * idea .test-projects/{test-name}
 * ```
 *
 * Option 2: Run Gradle via command line:
 * ```bash
 * cd .test-projects/{test-name}
 * ./gradlew ...
 * ```
 */
@DisplayNameGeneration(GradleVersionDisplayNameGenerator::class)
abstract class BaseGradleSpec {

    val rootProjectName = "test-project"

    /**
     * Gradle with the given version will be used in [GradleRunner]
     */
    var gradleVersion: GradleVersion = GradleVersions.GRADLE_VERSION_TO_TEST
    val buildEnvironment = mutableMapOf<String, String>()

    lateinit var mainBuild: GradleBuild
    val includedBuilds = mutableMapOf<String, GradleBuild>()

    /**
     * Set the full content of build.gradle.kts
     */
    fun buildGradleKts(configurator: GradleBuildscript.() -> Any) {
        mainBuild.buildGradleKts(configurator)
    }

    /**
     * Set the full content of [subprojectName]/build.gradle.kts and includes the subproject into the build
     */
    fun subprojectBuildGradleKts(subprojectName: String, configurator: GradleBuildscript.() -> Any) {
        mainBuild.subprojectBuildGradleKts(subprojectName, configurator)
    }

    /**
     * Set the full content of settings.gradle.kts
     */
    fun settingsGradleKts(configurator: GradleBuildscript.() -> Any) {
        mainBuild.settingsGradleKts(configurator)
    }

    /**
     * Register and configure included build
     */
    fun includedBuild(buildPath: String, configureBuild: GradleBuild.() -> Unit) {
        val build = includedBuilds.computeIfAbsent(buildPath) { mainBuild.registerIncludedBuild(buildPath) }
        build.configureBuild()
    }

    fun buildLogic(configureBuild: GradleBuild.() -> Unit) {
        includedBuild("build-logic", configureBuild)
    }

    /**
     * Register and configure buildSrc
     */
    fun buildSrc(configureBuild: GradleBuild.() -> Unit) {
        val build = includedBuilds.computeIfAbsent("buildSrc") {
            GradleBuild("buildSrc", mainBuild.rootDir.resolve("buildSrc"), gradleVersion)
        }
        build.configureBuild()
    }

    /**
     * Create the file under given [path] (relative to the test project root) with the given [content]
     */
    fun customProjectFile(path: String, content: () -> String): File {
        val file = mainBuild.rootDir.resolveOrCreate(path)
        file.writeText(content().trimIndent())
        return file
    }

    /**
     * Execute Gradle build in the temporary directory
     */
    protected fun runGradle(
        vararg arguments: String,
        customizer: GradleRunner.() -> Unit = {}
    ): SuccessOrFailureBuildResult =
        try {
            val args = buildList {
                addAll(arguments)
                add("--stacktrace")
                add("--configuration-cache")
            }

            dumpBuildEnvironment()

            GradleRunner.create()
                .withProjectDir(mainBuild.rootDir)
                .forwardOutput()
                .withGradleVersion(gradleVersion.version)
                .withEnvironment(System.getenv() + buildEnvironment)
                .withArguments(args)
                .apply(customizer)
                .build()
                .let { SuccessOrFailureBuildResult(it, BUILD_SUCCESSFUL) }
        } catch (e: UnexpectedBuildFailure) {
            SuccessOrFailureBuildResult(e.buildResult, BUILD_FAILED)
        }

    private fun dumpBuildEnvironment() {
        val envDump = buildEnvironment.entries
            .joinToString(separator = "\n") { (name, value) -> "systemProp.$name=${value.replace("\n", " \\n\\\n")}" }

        mainBuild.rootDir.resolveOrCreate("gradle.properties").writeText(envDump)
    }

    @BeforeEach
    fun beforeEach(testInfo: TestInfo) {
        val testProjectDir = Paths.get(".test-projects")
            .resolve(gradleVersion.version)
            .resolve(testInfo.testMethod.get().name.replace(" ", "-"))
            .toAbsolutePath().toFile()

        mainBuild = GradleBuild(
            rootProjectName = rootProjectName,
            rootDir = testProjectDir,
            gradleVersion = gradleVersion
        )
        setupGitHubContext()
    }

    private fun setupGitHubContext() {
        buildEnvironment["CI"] = "false" // to skip unshallowing in axion-release
    }

    companion object {
        @JvmStatic
        fun includedBuildConfigurators(): Stream<Arguments> {
            val notNestedBuildConfigurator: BuildConfigurator = {
                includedBuild("../not-nested-build-logic-for-${mainBuild.rootDir.name}", it)
            }

            return Stream.of(
                argumentSet("buildSrc", BaseGradleSpec::buildSrc),
                argumentSet("build-logic", BaseGradleSpec::buildLogic),
                argumentSet("not-nested-build-logic", notNestedBuildConfigurator),
            )
        }
    }
}

typealias BuildConfigurator = BaseGradleSpec.(GradleBuild.() -> Unit) -> Unit

class SuccessOrFailureBuildResult(
    private val delegate: BuildResult,
    val buildOutcome: BuildOutcome
) : BuildResult by delegate

enum class BuildOutcome {
    BUILD_SUCCESSFUL,
    BUILD_FAILED
}

val BuildResult.executedTasks: List<String>
    get() = tasks.map { it.path }

fun GradleRunner.doNotForwardOutput() {
    forwardStdOutput(StringWriter())
}