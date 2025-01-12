package dev.panuszewski.gradle.util

import org.gradle.api.Project
import java.io.File

internal fun Project.createNewFile(path: String): File {
    val file = file(path)
    file.parentFile.mkdirs()
    file.createNewFile()
    return file
}