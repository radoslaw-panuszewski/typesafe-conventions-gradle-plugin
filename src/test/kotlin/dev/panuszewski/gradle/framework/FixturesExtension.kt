package dev.panuszewski.gradle.framework

import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import java.lang.reflect.Method

class FixturesExtension : Extension, BeforeEachCallback, InvocationInterceptor {

    private val mutableInstalledFixtures = mutableListOf<Fixture<*>>()
    private lateinit var spec: GradleSpec
    private lateinit var includedBuild: BuildConfigurator

    val installedFixtures: List<Fixture<*>> = mutableInstalledFixtures

    fun <C : Any> installFixture(fixture: Fixture<C>, config: C) {
        if(mutableInstalledFixtures.contains(fixture)) {
            error("Fixture ${fixture.javaClass.simpleName} already installed!")
        }
        mutableInstalledFixtures.add(fixture)
        fixture.install(spec, includedBuild, config)
    }

    override fun beforeEach(context: ExtensionContext) {
        mutableInstalledFixtures.clear()
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