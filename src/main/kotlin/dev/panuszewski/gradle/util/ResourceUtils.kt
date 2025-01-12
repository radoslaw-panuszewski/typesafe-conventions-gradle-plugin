package dev.panuszewski.gradle.util

import dev.panuszewski.gradle.VersionCatalogAccessorsPlugin

fun readResourceAsString(name: String): String =
    VersionCatalogAccessorsPlugin::class.java.getResourceAsStream(name)
        ?.bufferedReader()
        ?.readText()
        ?: error("Unable to load resource $name, please report a bug on $GITHUB_ISSUES_URL")