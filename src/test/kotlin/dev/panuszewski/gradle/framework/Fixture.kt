package dev.panuszewski.gradle.framework

interface Fixture<T : Any> {
    fun GradleSpec.install(config: T)
    fun defaultConfig(): T
}
