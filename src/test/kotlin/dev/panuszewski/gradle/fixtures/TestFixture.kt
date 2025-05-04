package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.util.BaseGradleSpec
import dev.panuszewski.gradle.util.BuildConfigurator
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import java.lang.reflect.Method

abstract class TestFixture : Extension, BeforeEachCallback, InvocationInterceptor {

    protected lateinit var spec: BaseGradleSpec
    protected lateinit var includedBuild: BuildConfigurator

    override fun beforeEach(context: ExtensionContext) {
        spec = context.requiredTestInstance as? BaseGradleSpec
            ?: error("The ${javaClass.simpleName} extension can only be applied to subclasses of BaseGradleSpec")
    }

    override fun interceptTestMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        captureIncludedBuild(invocationContext)
        invocation.proceed()
    }

    override fun interceptTestTemplateMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext?
    ) {
        captureIncludedBuild(invocationContext)
        invocation.proceed()
    }

    private fun captureIncludedBuild(invocationContext: ReflectiveInvocationContext<Method>) {
        includedBuild = invocationContext.arguments
            .filterIsInstance<BuildConfigurator>()
            .firstOrNull()
            ?: BaseGradleSpec::buildSrc
    }

    abstract fun installFixture(): TestFixture
}