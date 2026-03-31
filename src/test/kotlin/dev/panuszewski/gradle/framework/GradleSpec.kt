package dev.panuszewski.gradle.framework

import dev.panuszewski.gradle.fixtures.includedbuild.BuildLogic
import dev.panuszewski.gradle.fixtures.includedbuild.BuildSrc
import dev.panuszewski.gradle.fixtures.includedbuild.NotNestedBuildLogic
import dev.panuszewski.gradle.framework.BuildOutcome.BUILD_SUCCESSFUL
import dev.panuszewski.gradle.util.gradleVersion
import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
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

    fun includedBuild(configureBuild: GradleBuild.() -> Unit) {
        singleIncludedBuild().configureBuild()
    }

    fun singleIncludedBuild(): GradleBuild {
        require(includedBuilds.size == 1) {
            "Required exactly 1 included build to be registered. Did you forgot to install a fixture like BuildSrc?"
        }
        return includedBuilds.values.first()
    }

    /**
     * Execute Gradle build in the temporary directory
     */
    protected fun runGradle(
        vararg arguments: String,
        customizer: GradleRunner.() -> Unit = {},
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
                .let { SuccessOrFailureBuildResult(it, BUILD_SUCCESSFUL) }
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
                argumentSet("buildSrc", BuildSrc),
                argumentSet("build-logic", BuildLogic),
                argumentSet("not-nested-build-logic", NotNestedBuildLogic)
            )
    }

    @MethodSource("allIncludedBuildTypes")
    @Target(FUNCTION)
    @Retention(RUNTIME)
    annotation class SupportedIncludedBuilds

    infix fun SuccessOrFailureBuildResult.shouldReportUnresolvedReference(reference: String) {
        if (gradleVersion >= GradleVersion.version("9.0.0")) {
            output shouldContain "Unresolved reference '$reference'"
        } else {
            output shouldContain "Unresolved reference: $reference"
        }
    }
}

class SuccessOrFailureBuildResult(
    private val delegate: BuildResult,
    val buildOutcome: BuildOutcome,
) : BuildResult by delegate

enum class BuildOutcome {
    BUILD_SUCCESSFUL,
    BUILD_FAILED,
}

fun SuccessOrFailureBuildResult.shouldSucceed() {
    this.buildOutcome shouldBe BUILD_SUCCESSFUL
}

fun SuccessOrFailureBuildResult.shouldFail() {
    this.buildOutcome shouldBe BuildOutcome.BUILD_FAILED
}

fun shouldAllBuildsSucceed(vararg buildResults: SuccessOrFailureBuildResult) {
    buildResults.shouldForAll { it.buildOutcome shouldBe BUILD_SUCCESSFUL }
}
