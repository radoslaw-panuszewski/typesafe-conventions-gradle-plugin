package dev.panuszewski.gradle.util

internal fun Any.readResourceAsString(name: String): String =
    javaClass.getResourceAsStream(name)
        ?.bufferedReader()
        ?.readText()
        ?: error("Unable to load resource $name, please report a bug on $GITHUB_ISSUES_URL")