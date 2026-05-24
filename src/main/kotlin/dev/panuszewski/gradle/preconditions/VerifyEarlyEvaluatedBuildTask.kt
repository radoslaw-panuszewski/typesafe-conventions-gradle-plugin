package dev.panuszewski.gradle.preconditions

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

@CacheableTask
internal abstract class VerifyEarlyEvaluatedBuildTask : DefaultTask() {

    @get:Input
    abstract val earlyEvaluatedBuild: Property<Boolean>

    @get:Input
    abstract val buildName: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

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
                """.trimIndent(),
            )
        } else {
            outputFile.get().asFile.writeText("OK")
        }
    }
}
