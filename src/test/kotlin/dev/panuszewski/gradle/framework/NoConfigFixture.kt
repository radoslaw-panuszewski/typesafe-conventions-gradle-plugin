package dev.panuszewski.gradle.framework

interface NoConfigFixture : Fixture<Unit> {

    override fun defaultConfig() = Unit

    override fun GradleSpec.install(includedBuild: BuildConfigurator, config: Unit) {
        install(includedBuild)
    }

    fun GradleSpec.install(includedBuild: BuildConfigurator)
}