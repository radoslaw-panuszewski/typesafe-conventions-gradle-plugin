package dev.panuszewski.gradle

import org.gradle.api.internal.GradleInternal
import org.gradle.util.Path

internal class BuildHierarchy(rootBuild: GradleInternal) {

    private val directBuildParents: Map<Path, GradleInternal>

    init {
        directBuildParents = directBuildParentsOf(rootBuild)
    }

    fun directParentOf(gradle: GradleInternal): GradleInternal? = directBuildParents[gradle.identityPath]

    private fun directBuildParentsOf(rootProject: GradleInternal): Map<Path, GradleInternal> {
        fun MutableMap<Path, GradleInternal>.putDirectParentsOf(currentProject: GradleInternal) {
            for (childBuild in currentProject.includedBuilds()) {
                val childProject = childBuild.target.mutableModel
                if (putIfAbsent(childProject.identityPath, currentProject) == null) {
                    putDirectParentsOf(childProject)
                }
            }
        }
        return buildMap {
            putDirectParentsOf(rootProject)
        }
    }
}
