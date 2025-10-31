package dev.panuszewski.gradle

import dev.panuszewski.gradle.catalog.BuilderCatalogContributor
import dev.panuszewski.gradle.catalog.CatalogAccessorsPlugin
import dev.panuszewski.gradle.catalog.CatalogContributor
import dev.panuszewski.gradle.catalog.TomlCatalogContributor
import dev.panuszewski.gradle.util.currentGradleVersion
import dev.panuszewski.gradle.util.gradleVersionAtLeast
import dev.panuszewski.gradle.util.pathString
import dev.panuszewski.gradle.verification.LazyVerificationPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.internal.management.VersionCatalogBuilderInternal
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.newInstance
import java.io.File
import javax.inject.Inject

@Suppress("unused") // used as a plugin implementation class
internal class TypesafeConventionsPlugin @Inject constructor(
    private val objects: ObjectFactory
) : Plugin<Any> {

    override fun apply(target: Any) {
        require(gradleVersionAtLeast(MINIMAL_GRADLE_VERSION)) { mustUseMinimalGradleVersion() }

        val settings = target as? Settings ?: mustBeAppliedToSettings(target)
        registerExtension(settings)
        settings.apply<LazyVerificationPlugin>()

        val currentBuild = settings.gradle as GradleInternal
        val parentBuild = currentBuild.parent

        if (currentBuild.identityPath.pathString.endsWith(":buildSrc")) {
            // buildSrc does not participate in hierarchy flattening
            configure(settings, parentBuild)
        }
        if (parentBuild != null) {
            // build hierarchy is flattened by Gradle
            val rootBuild = parentBuild

            rootBuild.projectsLoaded {
                val buildHierarchy = BuildHierarchy(rootBuild)
                val directParentBuild = buildHierarchy.directParentOf(currentBuild)
                configure(settings, directParentBuild)
            }
        } else {
            configure(settings, null)
        }
    }

    private fun configure(settings: Settings, parentBuild: GradleInternal?) {
        if (parentBuild != null) {
            inheritCatalogsFromParentBuild(settings, parentBuild)
        }
        applySubPluginsForAllProjects(settings)
    }

    private fun registerExtension(settings: Settings) {
        settings.extensions.create<TypesafeConventionsExtension>("typesafeConventions")
    }

    private fun inheritCatalogsFromParentBuild(settings: Settings, parentBuild: GradleInternal) {
        val builders = discoverBuilders(parentBuild)
        val tomlFiles = discoverTomlFiles(settings, parentBuild)

        val contributorsByName = builders.associateBy(CatalogContributor::catalogName).toMutableMap()

        tomlFiles.forEach { tomlFile -> contributorsByName.computeIfAbsent(tomlFile.catalogName) { tomlFile } }

        contributorsByName.values.forEach { it.contributeTo(settings.dependencyResolutionManagement.versionCatalogs) }
    }

    private fun discoverBuilders(parentBuild: GradleInternal): List<CatalogContributor> =
        try {
            val catalogBuilders = parentBuild.settings
                .dependencyResolutionManagement
                .dependenciesModelBuilders
                .filterIsInstance<VersionCatalogBuilderInternal>()

            catalogBuilders.map { builder -> objects.newInstance<BuilderCatalogContributor>(builder) }
        } catch (e: IllegalStateException) {
            emptyList()
        }

    private fun discoverTomlFiles(settings: Settings, parentBuild: GradleInternal): List<CatalogContributor> {
        val parentGradleDir = resolveParentGradleDir(parentBuild, settings)

        val tomlFiles = parentGradleDir
            .walk()
            .filter { it.name.endsWith(".versions.toml") }
            .toList()

        return tomlFiles.map { tomlFile -> objects.newInstance<TomlCatalogContributor>(tomlFile) }
    }

    private fun resolveParentGradleDir(parentBuild: GradleInternal, settings: Settings): File =
        try {
            parentBuild.settings.rootDir.resolve("gradle")
        } catch (e: Throwable) {
            settings.rootDir.resolve("..").resolve("gradle")
        }

    private fun applySubPluginsForAllProjects(settings: Settings) {
        settings.gradle.rootProject {
            allprojects {
                apply<CatalogAccessorsPlugin>()
            }
        }
    }

    // TODO use GradlePluginApiVersion attribute instead
    private fun mustUseMinimalGradleVersion(): Nothing {
        error(
            "The typesafe-conventions plugin requires Gradle version at least $MINIMAL_GRADLE_VERSION, " +
                "but currently ${currentGradleVersion()} is used."
        )
    }

    private fun mustBeAppliedToSettings(target: Any): Nothing {
        val buildFileKind = when (target) {
            is Project -> "build.gradle.kts"
            is Gradle -> "init script"
            else -> target::class.simpleName
        }
        error(
            "The typesafe-conventions plugin must be applied to settings.gradle.kts, " +
                "but attempted to apply it to $buildFileKind"
        )
    }

    companion object {
        private val logger = Logging.getLogger(TypesafeConventionsPlugin::class.java)
        internal const val MINIMAL_GRADLE_VERSION = "8.7"
    }
}
