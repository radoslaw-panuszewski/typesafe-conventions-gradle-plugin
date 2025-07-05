package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.fixtures.TypesafeConventionsConfig.Config
import dev.panuszewski.gradle.framework.AppendableFile
import dev.panuszewski.gradle.framework.Fixture
import dev.panuszewski.gradle.framework.GradleBuild
import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.util.gradleVersion

object TypesafeConventionsConfig : Fixture<Config> {

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
                appendLine { "typesafeConventions {" }
                appendEachProperty(config)
                appendLine { "}" }
            } else {
                prependLine { "import dev.panuszewski.gradle.TypesafeConventionsExtension" }
                appendLine { "configure<TypesafeConventionsExtension> {" }
                appendEachProperty(config)
                appendLine { "}" }
            }
        }
    }

    private fun AppendableFile.appendEachProperty(config: Config) {
        config.accessorsInPluginsBlock?.let { appendLine { "accessorsInPluginsBlock = $it" } }
        config.autoPluginDependencies?.let { appendLine { "autoPluginDependencies = $it" } }
        config.allowTopLevelBuild?.let { appendLine { "allowTopLevelBuild = $it" } }
        config.suppressPluginManagementIncludedBuildWarning?.let { appendLine { "suppressPluginManagementIncludedBuildWarning = $it" } }
    }

    override fun defaultConfig() = Config()

    class Config {
        var accessorsInPluginsBlock: Boolean? = null
        var autoPluginDependencies: Boolean? = null
        var allowTopLevelBuild: Boolean? = null
        var suppressPluginManagementIncludedBuildWarning: Boolean? = null
    }
}