package dev.panuszewski.gradle.framework

import java.io.File

fun File.resolveOrCreate(fileName: String): File {
    val file = resolve(fileName)
    file.parentFile.mkdirs()
    file.createNewFile()
    return file
}
