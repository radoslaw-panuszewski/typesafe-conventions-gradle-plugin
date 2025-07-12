package dev.panuszewski.gradle.verification

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property

internal abstract class VerifyEarlyEvaluatedBuildTask : DefaultTask() {

    @Input
    val earlyEvaluatedBuild: Property<Boolean> = project.objects.property()

    @Input
    val buildName: Property<String> = project.objects.property()

    @TaskAction
    fun execute() {
        if (earlyEvaluatedBuild.get()) {
            error(
                """
                
                The typesafe-conventions plugin is applied to an early-evaluated included build!
                This kind of builds are not supported, because they are not aware of the build hierarchy.
                
                To fix this issue, replace this code in your settings.gradle.kts:
                
                pluginManagement {
                    includeBuild("${buildName.get()}")
                }
                
                with this:
                
                includeBuild("${buildName.get()}")
                """.trimIndent()
            )
        }
    }
}