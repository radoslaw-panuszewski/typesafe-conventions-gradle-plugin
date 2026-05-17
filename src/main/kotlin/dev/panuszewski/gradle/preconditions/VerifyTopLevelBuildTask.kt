package dev.panuszewski.gradle.preconditions

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

@CacheableTask
internal abstract class VerifyTopLevelBuildTask : DefaultTask() {

    @get:Input
    abstract val topLevelBuild: Property<Boolean>

    @get:Input
    abstract val allowTopLevelBuild: Property<Boolean>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

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
                """.trimIndent(),
            )
        } else {
            outputFile.get().asFile.writeText("OK")
        }
    }
}
