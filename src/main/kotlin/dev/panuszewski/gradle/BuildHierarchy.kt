package dev.panuszewski.gradle

import org.gradle.api.internal.GradleInternal
import org.gradle.api.invocation.Gradle

internal class BuildHierarchy(rootBuild: GradleInternal) {

    private val directBuildParents: Map<GradleInternal, GradleInternal>

    init {
        directBuildParents = directBuildParentsOf(rootBuild)
    }

    fun directParentOf(gradle: Gradle): GradleInternal? = directBuildParents[gradle]

    private fun directBuildParentsOf(parentBuild: GradleInternal): Map<GradleInternal, GradleInternal> = buildMap {
        parentBuild.includedBuilds()
            .map { it.target.mutableModel }
            .forEach { includedBuild ->
                put(includedBuild, parentBuild)
                putAll(directBuildParentsOf(includedBuild))
            }
    }
}
