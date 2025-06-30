package dev.panuszewski.gradle.framework

interface Fixture<T : Any> {
    fun GradleSpec.install(includedBuild: BuildConfigurator, config: T)
    fun defaultConfig(): T
}
