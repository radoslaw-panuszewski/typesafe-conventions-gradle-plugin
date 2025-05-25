package dev.panuszewski.gradle.framework

interface NoConfigFixture : Fixture<Unit> {

    override fun defaultConfig() = Unit

    override fun install(spec: GradleSpec, includedBuild: BuildConfigurator, config: Unit) {
        install(spec, includedBuild)
    }

    fun install(spec: GradleSpec, includedBuild: BuildConfigurator)
}