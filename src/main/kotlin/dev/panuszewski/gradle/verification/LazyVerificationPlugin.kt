package dev.panuszewski.gradle.verification

import dev.panuszewski.gradle.KOTLIN_GRADLE_PLUGIN_ID
import dev.panuszewski.gradle.util.typesafeConventions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

/**
 * The purpose of this plugin is to verify usage correctness lazily (at execution phase)
 *
 * For example, we don't want to verify top level build eagerly since some tools (like Android Studio)
 * sometimes execute tasks (like clean) on the included build in isolation, so without a parent.
 */
internal class LazyVerificationPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.withId(KOTLIN_GRADLE_PLUGIN_ID) {

            project.tasks.register<VerifyTopLevelBuildTask>("verifyTopLevelBuild") {
                topLevelBuild.set(project.gradle.parent == null)
                allowTopLevelBuild.set(project.typesafeConventions.allowTopLevelBuild)
            }

            project.tasks.named("compileKotlin") {
                dependsOn("verifyTopLevelBuild")
            }
        }
    }
}