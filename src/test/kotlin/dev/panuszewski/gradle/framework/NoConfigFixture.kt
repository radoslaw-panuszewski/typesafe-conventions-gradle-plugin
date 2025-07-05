package dev.panuszewski.gradle.framework

interface NoConfigFixture : Fixture<Unit> {

    override fun defaultConfig() = Unit

    override fun GradleSpec.install(config: Unit) {
        install()
    }

    fun GradleSpec.install()
}