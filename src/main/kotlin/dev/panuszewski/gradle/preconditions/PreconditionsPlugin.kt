package dev.panuszewski.gradle.preconditions

import dev.panuszewski.gradle.TypesafeConventionsPlugin.Companion.KOTLIN_GRADLE_PLUGIN_ID
import dev.panuszewski.gradle.util.typesafeConventions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

/**
 * The purpose of this plugin is to verify usage correctness lazily (at execution phase)
 *
 * For example, we don't want to verify top level build eagerly since some tools (like Android Studio)
 * sometimes execute tasks (like clean) on the included build in isolation, so without a parent.
 */
internal class PreconditionsPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        val isEarlyEvaluatedBuild = settings.isEarlyEvaluatedIncludedBuild()

        settings.gradle.rootProject {
            allprojects {
                project.plugins.withId(KOTLIN_GRADLE_PLUGIN_ID) {
                    registerTasks(isEarlyEvaluatedBuild)
                }
            }
        }
    }

    private fun Project.registerTasks(isEarlyEvaluatedBuild: Boolean) {
        val validateTopLevelBuild = registerVerifyTopLevelBuildTask()
        val verifyEarlyEvaluatedBuild = registerVerifyEarlyEvaluatedBuildTask(isEarlyEvaluatedBuild)

        project.tasks.named("compileKotlin") {
            dependsOn(validateTopLevelBuild, verifyEarlyEvaluatedBuild)
        }
    }

    private fun Project.registerVerifyTopLevelBuildTask(): TaskProvider<*> =
        tasks.register<VerifyTopLevelBuildTask>("verifyTopLevelBuild") {
            topLevelBuild.set(gradle.parent == null)
            allowTopLevelBuild.set(typesafeConventions.allowTopLevelBuild)
        }

    private fun Project.registerVerifyEarlyEvaluatedBuildTask(isEarlyEvaluatedBuild: Boolean): TaskProvider<*> =
        tasks.register<VerifyEarlyEvaluatedBuildTask>("verifyEarlyEvaluatedBuild") {
            earlyEvaluatedBuild.set(isEarlyEvaluatedBuild)
            buildName.set(rootProject.name)
        }
}
