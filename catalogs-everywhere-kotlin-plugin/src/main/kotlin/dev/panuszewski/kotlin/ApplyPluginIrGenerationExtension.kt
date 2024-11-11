package dev.panuszewski.kotlin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrScript
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.util.sourceElement
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.util.Logger

class ApplyPluginIrGenerationExtension(
    private val logger: Logger
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.accept(Visitor(), null)
    }

    private inner class Visitor : IrElementVisitorVoid {

        override fun visitElement(element: IrElement) {
            element.acceptChildren(this, null)
        }

        override fun visitFile(file: IrFile) {
            super.visitFile(file)
            if (file.name == "some-client-convention.gradle.kts") {
                val script = file.declarations.first() as IrScript
                logger.warning(script.statements.joinToString(separator = "\n"))
            }
        }
    }
}