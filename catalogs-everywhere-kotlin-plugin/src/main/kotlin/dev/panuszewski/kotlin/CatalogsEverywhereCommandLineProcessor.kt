@file:OptIn(ExperimentalCompilerApi::class)

package dev.panuszewski.kotlin

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

class CatalogsEverywhereCommandLineProcessor : CommandLineProcessor {

    override val pluginId = "dev.panuszewski.catalogs-everywhere"

    override val pluginOptions: Collection<AbstractCliOption> = emptyList()
}