package dev.panuszewski.gradle.framework

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayNameGeneration
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import java.io.StringWriter
import java.nio.file.Paths
import java.util.stream.Stream
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION

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
abstract class GradleSpec {

    val rootProjectName = "test-project"

    /**
     * Gradle with the given version will be used in [GradleRunner]
     */
    var gradleVersion: GradleVersion = GradleVersions.GRADLE_VERSION_TO_TEST
    val projectVersion: String = System.getenv("PROJECT_VERSION")
    val buildEnvironment = mutableMapOf<String, String>()

    lateinit var mainBuild: GradleBuild
    val includedBuilds = mutableMapOf<String, GradleBuild>()

    var configurationCacheEnabled = true
    var buildCacheEnabled = true

    @RegisterExtension
    val fixtures = FixturesExtension()

    fun <T : Fixture<C>, C : Any> installFixture(fixture: T, configure: C.() -> Unit = {}): T {
        val config = fixture.defaultConfig()
        config.configure()
        fixtures.installFixture(fixture, config)
        return fixture
    }

    /**
     * Override, append or prepend content of `build.gradle.kts`
     */
    fun buildGradleKts(configurator: AppendableFile.() -> Any) {
        mainBuild.buildGradleKts(configurator)
    }

    /**
     * Override, append or prepend content of `<subprojectName>/build.gradle.kts` and include the subproject in the build
     */
    fun subprojectBuildGradleKts(subprojectName: String, configurator: AppendableFile.() -> Any) {
        mainBuild.subprojectBuildGradleKts(subprojectName, configurator)
    }

    /**
     * Override, append or prepend content of `settings.gradle.kts`
     */
    fun settingsGradleKts(configurator: AppendableFile.() -> Any) {
        mainBuild.settingsGradleKts(configurator)
    }

    /**
     * Override, append or prepend content of `gradle/libs.versions.toml`
     */
    fun libsVersionsToml(configurator: AppendableFile.() -> Any) {
        mainBuild.libsVersionsToml(configurator)
    }

    /**
     * Override, append or prepend content of a custom file under [path]
     */
    fun customProjectFile(path: String, configurator: AppendableFile.() -> Any) {
        mainBuild.customProjectFile(path, configurator)
    }

    /**
     * Register and configure included build
     */
    fun includedBuild(buildPath: String, configureBuild: GradleBuild.() -> Unit) {
        val build = includedBuilds.computeIfAbsent(buildPath) {
            mainBuild.registerIncludedBuild(buildPath)
        }
        build.configureBuild()
    }

    fun pluginManagementIncludedBuild(buildPath: String, configureBuild: GradleBuild.() -> Unit) {
        val build = includedBuilds.computeIfAbsent(buildPath) {
            mainBuild.registerPluginManagementIncludedBuild(buildPath)
        }
        build.configureBuild()
    }

    /**
     * Register and configure build-logic included build
     */
    fun buildLogic(configureBuild: GradleBuild.() -> Unit) {
        includedBuild("build-logic", configureBuild)
    }

    fun pluginManagementBuildLogicWithAtLeastOneSettingsPlugin(configureBuild: GradleBuild.() -> Unit) {
        pluginManagementIncludedBuild("build-logic", configureBuild)
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

    fun notNestedBuildLogic(configureBuild: GradleBuild.() -> Unit) {
        includedBuild("../build-logic-for-${mainBuild.rootDir.name}", configureBuild)
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
                if (configurationCacheEnabled) add("--configuration-cache")
                if (buildCacheEnabled) add("--build-cache")
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
                .let { SuccessOrFailureBuildResult(it, BuildOutcome.BUILD_SUCCESSFUL) }
        } catch (e: UnexpectedBuildFailure) {
            SuccessOrFailureBuildResult(e.buildResult, BuildOutcome.BUILD_FAILED)
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
        @Suppress("unused") // used in @AllIncludedBuildTypes
        @JvmStatic
        fun allIncludedBuildTypes(): Stream<Arguments> =
            Stream.of(
                argumentSet("buildSrc", GradleSpec::buildSrc),
                argumentSet("build-logic", GradleSpec::buildLogic),
                argumentSet("plugin-management-build-logic", GradleSpec::pluginManagementBuildLogicWithAtLeastOneSettingsPlugin),
                argumentSet("not-nested-build-logic", GradleSpec::notNestedBuildLogic),
            )
    }

    @MethodSource("allIncludedBuildTypes")
    @Target(FUNCTION)
    @Retention(RUNTIME)
    annotation class AllIncludedBuildTypes
}

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