@file:OptIn(ExperimentalCompilerApi::class)

package dev.panuszewski.kotlin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.getLogger
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

class CatalogsEverywhereComponentRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val extension = ApplyPluginIrGenerationExtension(logger = configuration.getLogger())
        IrGenerationExtension.registerExtension(extension)
    }
}