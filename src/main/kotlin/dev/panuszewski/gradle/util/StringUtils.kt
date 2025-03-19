package dev.panuszewski.gradle.util

internal val String.capitalized: String
    get() = replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }