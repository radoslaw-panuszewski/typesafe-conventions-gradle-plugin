package dev.panuszewski.gradle.verification

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property

internal abstract class VerifyTopLevelBuildTask : DefaultTask() {

    @Input
    val topLevelBuild: Property<Boolean> = project.objects.property()

    @Input
    val allowTopLevelBuild: Property<Boolean> = project.objects.property()

    @TaskAction
    fun execute() {
        if (topLevelBuild.get() && !allowTopLevelBuild.get()) {
            error(
                """
                The typesafe-conventions plugin is applied to a top-level build, but in most cases it should be applied to an included build or buildSrc. 
                If you know what you're doing, allow top-level build in your settings.gradle.kts:
    
                typesafeConventions { 
                    allowTopLevelBuild = true 
                }
    
                Read more here: https://github.com/radoslaw-panuszewski/typesafe-conventions-gradle-plugin/blob/main/README.md#top-level-build
                """.trimIndent()
            )
        }
    }
}