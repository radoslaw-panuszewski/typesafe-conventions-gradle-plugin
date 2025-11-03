package dev.panuszewski.gradle.buildstructure

import dev.panuszewski.gradle.util.pathString
import dev.panuszewski.gradle.util.root
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.SettingsInternal
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

internal abstract class BuildFactory @Inject constructor(
    settings: SettingsInternal,
    private val objects: ObjectFactory
) {
    private val directBuildParents by lazy {
        buildMap { putDirectParentsOf(settings.gradle.root()) }
    }

    private fun MutableMap<GradleInternal, GradleInternal>.putDirectParentsOf(currentBuild: GradleInternal) {
        for (childBuild in currentBuild.includedBuilds()) {
            val childProject = childBuild.target.mutableModel
            if (putIfAbsent(childProject, currentBuild) == null) {
                putDirectParentsOf(childProject)
            }
        }
    }

    fun createBuildWhenReady(gradle: GradleInternal, settings: SettingsInternal, buildConsumer: (Build) -> Unit) {
        if (gradle.identityPath.pathString.endsWith(":buildSrc")) {
            // buildSrc does not participate in hierarchy flattening
            val build = createBuildSrc(gradle, settings)
            buildConsumer.invoke(build)
        } else {
            // build hierarchy is flattened by Gradle
            gradle.root().projectsLoaded {
                val build = createRegularBuild(gradle, settings)
                buildConsumer.invoke(build)
            }
        }
    }

    private fun createBuildSrc(gradle: GradleInternal, settings: SettingsInternal = gradle.settings): Build =
        Build(
            gradle = gradle,
            settings = settings,
            parentProvider = { gradle.parent?.let(::createRegularBuild) },
            objects = objects
        )

    private fun createRegularBuild(gradle: GradleInternal, settings: SettingsInternal = gradle.settings): Build =
        Build(
            gradle = gradle,
            settings = settings,
            parentProvider = { directBuildParents[gradle]?.let(::createRegularBuild) },
            objects = objects
        )
}
