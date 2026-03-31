package dev.panuszewski.gradle

import dev.panuszewski.gradle.conventioncatalogs.ConventionCatalogPlugin
import dev.panuszewski.gradle.conventioncatalogs.ConventionCatalogPlugin.Companion.CONVENTION_CATALOG_CONFIG_KEY
import dev.panuszewski.gradle.parentbuild.ParentBuild
import dev.panuszewski.gradle.parentbuild.ParentBuildResolver
import dev.panuszewski.gradle.preconditions.PreconditionsPlugin
import dev.panuszewski.gradle.preconditions.isEarlyEvaluatedIncludedBuild
import dev.panuszewski.gradle.util.currentGradleVersion
import dev.panuszewski.gradle.util.gradleVersionAtLeast
import dev.panuszewski.gradle.util.pathString
import dev.panuszewski.gradle.util.typesafeConventions
import dev.panuszewski.gradle.versioncatalogs.VersionCatalogAccessorsPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.SettingsInternal
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

@Suppress("unused")
internal class TypesafeConventionsPlugin @Inject constructor(
    private val objects: ObjectFactory,
) : Plugin<Any> {

    override fun apply(target: Any) {
        require(gradleVersionAtLeast(MINIMAL_GRADLE_VERSION)) { mustUseMinimalGradleVersion() }

        val settings = target as? SettingsInternal ?: mustBeAppliedToSettings(target)
        registerExtension(settings)
        settings.apply<PreconditionsPlugin>()

        if (settings.isEarlyEvaluatedIncludedBuild()) {
            // error will be thrown lazily in execution phase
            return
        }

        settings.gradle.settingsEvaluated {
            if (settings.gradle.identityPath.pathString.count { it == ':' } == 1) {
                settings.gradle.parent?.let { flattenedParentBuild ->
                    applyConventionCatalogPlugin(flattenedParentBuild.settings, settings.typesafeConventions)
                }
            }

            resolveParentBuild(settings) { parentBuild ->
                if (parentBuild != null) {
                    applyConventionCatalogPlugin(parentBuild.settings, settings.typesafeConventions)
                    importVersionCatalogsFromParentBuild(parentBuild, settings)
                }
                applyVersionCatalogAccessorsPlugin(settings)
            }
        }
    }

    private fun registerExtension(settings: Settings) {
        settings.extensions.create<TypesafeConventionsExtension>("typesafeConventions")
    }

    private fun applyConventionCatalogPlugin(settings: Settings, extension: TypesafeConventionsExtension) {
        settings.extra[CONVENTION_CATALOG_CONFIG_KEY] = extension.conventionCatalog
        settings.apply<ConventionCatalogPlugin>()
    }

    private fun resolveParentBuild(settings: SettingsInternal, consumer: (ParentBuild?) -> Unit) {
        val resolver = objects.newInstance<ParentBuildResolver>()
        resolver.resolveParentBuild(settings.gradle, consumer)
    }

    private fun importVersionCatalogsFromParentBuild(parentBuild: ParentBuild, settings: Settings) {
        parentBuild.versionCatalogs.forEach { it.importTo(settings) }
    }

    private fun applyVersionCatalogAccessorsPlugin(settings: Settings) {
        settings.gradle.rootProject {
            allprojects {
                apply<VersionCatalogAccessorsPlugin>()
            }
        }
    }

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
        internal const val MINIMAL_GRADLE_VERSION = "8.8"
        internal const val KOTLIN_DSL_PLUGIN_ID = "org.gradle.kotlin.kotlin-dsl"
    }
}
