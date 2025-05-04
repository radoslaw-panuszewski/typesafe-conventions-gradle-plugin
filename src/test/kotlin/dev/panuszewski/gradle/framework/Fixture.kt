package dev.panuszewski.gradle.framework

interface Fixture<T : Any> {
    fun install(spec: GradleSpec, includedBuild: BuildConfigurator, config: T)
    fun defaultConfig(): T
}
