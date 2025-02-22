package dev.panuszewski.gradle.util

import dev.panuszewski.gradle.catalog.CatalogAccessorsPlugin

internal fun readResourceAsString(name: String): String =
    CatalogAccessorsPlugin::class.java.getResourceAsStream(name)
        ?.bufferedReader()
        ?.readText()
        ?: error("Unable to load resource $name, please report a bug on $GITHUB_ISSUES_URL")