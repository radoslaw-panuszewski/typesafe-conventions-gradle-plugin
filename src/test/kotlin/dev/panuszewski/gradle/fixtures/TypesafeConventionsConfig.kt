package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.fixtures.TypesafeConventionsConfig.Config
import dev.panuszewski.gradle.framework.AppendableFile
import dev.panuszewski.gradle.framework.Fixture
import dev.panuszewski.gradle.framework.GradleBuild
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.util.gradleVersion

object TypesafeConventionsConfig : Fixture<Config> {

    private val INDENT = " ".repeat(4)
    private val DOUBLE_INDENT = " ".repeat(8)

    override fun GradleSpec.install(config: Config) {
        when {
            fixtures.installedFixtures.contains(TypesafeConventionsAppliedToIncludedBuild) -> {
                includedBuild { applyConfiguration(config) }
            }
            fixtures.installedFixtures.contains(TypesafeConventionsAppliedToTopLevelBuild) -> {
                mainBuild.applyConfiguration(config)
            }
            else -> {
                error("Can't install TypesafeConventionsConfig since typesafe-conventions plugin is not applied")
            }
        }
    }

    private fun GradleBuild.applyConfiguration(config: Config) {
        settingsGradleKts {
            if (gradleVersion >= gradleVersion("8.8")) {
                append { "typesafeConventions {" }
                appendLine()
                appendEachProperty(config)
                appendLine { "}" }
            } else {
                prepend { "import dev.panuszewski.gradle.TypesafeConventionsExtension" }
                append { "configure<TypesafeConventionsExtension> {" }
                appendLine()
                appendEachProperty(config)
                appendLine { "}" }
            }
        }
    }

    private fun AppendableFile.appendEachProperty(config: Config) {
        config.accessorsInPluginsBlock?.let { appendLine { "${INDENT}accessorsInPluginsBlock = $it" } }
        config.autoPluginDependencies?.let { appendLine { "${INDENT}autoPluginDependencies = $it" } }
        config.allowTopLevelBuild?.let { appendLine { "${INDENT}allowTopLevelBuild = $it" } }
        appendLine { "${INDENT}conventionCatalog {" }
        config.conventionCatalogName?.let { appendLine { "${DOUBLE_INDENT}catalogName = \"$it\"" } }
        appendLine { "$INDENT}" }
    }

    override fun defaultConfig() = Config()

    class Config {
        var accessorsInPluginsBlock: Boolean? = null
        var autoPluginDependencies: Boolean? = null
        var allowTopLevelBuild: Boolean? = null
        var conventionCatalogName: String? = null
    }
}
