package dev.panuszewski.gradle.parentbuild

import dev.panuszewski.gradle.util.pathString
import org.gradle.api.internal.GradleInternal
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

internal abstract class ParentBuildResolver @Inject constructor(
    private val objects: ObjectFactory
) {
    /**
     * Gradle flattens the hierarchy of composite builds, so the root build is a parent
     * to every included build (no matter how deeply nested). This method rebuilds
     * the original hierarchy by recursing included builds of the root build.
     * Those included builds are not available straightaway, so we need to use
     * the projectsLoaded { ... } callback.
     *
     * Unlike regular included builds, buildSrc retains its original parent,
     * thus it's handled in a special way.
     */
    fun resolveParentBuild(gradle: GradleInternal, consumer: (ParentBuild?) -> Unit) {
        if (gradle.identityPath.pathString.endsWith(":buildSrc")) {
            val parentBuild = gradle.parent?.let(::parentBuild)
            consumer.invoke(parentBuild)
        } else {
            gradle.root().projectsLoaded {
                val directBuildParents = buildMap { putDirectChildrenOf(gradle.root()) }
                val parentBuild = directBuildParents[gradle]?.let(::parentBuild)
                consumer.invoke(parentBuild)
            }
        }
    }

    private fun GradleInternal.root(): GradleInternal =
        parent?.root() ?: this

    private fun MutableMap<GradleInternal, GradleInternal>.putDirectChildrenOf(currentBuild: GradleInternal) {
        for (childBuild in currentBuild.includedBuilds()) {
            val childProject = childBuild.target.mutableModel
            if (putIfAbsent(childProject, currentBuild) == null) {
                putDirectChildrenOf(childProject)
            }
        }
    }

    private fun parentBuild(gradle: GradleInternal): ParentBuild =
        objects.newInstance<ParentBuild>(gradle)
}
