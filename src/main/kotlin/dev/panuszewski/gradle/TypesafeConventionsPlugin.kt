package dev.panuszewski.gradle

import dev.panuszewski.gradle.parentbuild.ParentBuild
import dev.panuszewski.gradle.parentbuild.ParentBuildResolver
import dev.panuszewski.gradle.preconditions.PreconditionsPlugin
import dev.panuszewski.gradle.util.currentGradleVersion
import dev.panuszewski.gradle.util.gradleVersionAtLeast
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
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

@Suppress("unused")
internal class TypesafeConventionsPlugin @Inject constructor(
    private val objects: ObjectFactory
) : Plugin<Any> {

    override fun apply(target: Any) {
        require(gradleVersionAtLeast(MINIMAL_GRADLE_VERSION)) { mustUseMinimalGradleVersion() }

        val settings = target as? SettingsInternal ?: mustBeAppliedToSettings(target)
        registerExtension(settings)
        applyPreconditionsPlugin(settings)

        resolveParentBuild(settings) { parentBuild ->
            if (parentBuild != null) {
                importVersionCatalogsFromParentBuild(parentBuild, settings)
            }
            applyVersionCatalogAccessorsPlugin(settings)
        }
    }

    private fun registerExtension(settings: Settings) {
        settings.extensions.create<TypesafeConventionsExtension>("typesafeConventions")
    }

    private fun applyPreconditionsPlugin(settings: Settings) {
        settings.apply<PreconditionsPlugin>()
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
        internal const val MINIMAL_GRADLE_VERSION = "8.7"
        internal const val KOTLIN_GRADLE_PLUGIN_ID = "org.jetbrains.kotlin.jvm"
    }
}
