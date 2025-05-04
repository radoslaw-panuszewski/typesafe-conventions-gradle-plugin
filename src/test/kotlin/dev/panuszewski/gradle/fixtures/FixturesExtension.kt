package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.util.GradleSpec
import dev.panuszewski.gradle.util.BuildConfigurator
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import java.lang.reflect.Method

class FixturesExtension : Extension, BeforeEachCallback, InvocationInterceptor {

    private lateinit var spec: GradleSpec
    private lateinit var includedBuild: BuildConfigurator

    fun installFixture(fixture: Fixture) {
        fixture.install(spec, includedBuild)
    }

    override fun beforeEach(context: ExtensionContext) {
        spec = context.requiredTestInstance as? GradleSpec
            ?: error("The ${javaClass.simpleName} extension can only be applied to subclasses of BaseGradleSpec")
    }

    override fun interceptTestMethod(
        invocation: Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        captureIncludedBuild(invocationContext)
        invocation.proceed()
    }

    override fun interceptTestTemplateMethod(
        invocation: Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        captureIncludedBuild(invocationContext)
        invocation.proceed()
    }

    private fun captureIncludedBuild(invocationContext: ReflectiveInvocationContext<Method>) {
        includedBuild = invocationContext.arguments
            .filterIsInstance<BuildConfigurator>()
            .firstOrNull()
            ?: GradleSpec::buildSrc
    }
}