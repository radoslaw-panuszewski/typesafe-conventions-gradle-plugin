package dev.panuszewski.gradle.framework

interface NoConfigFixture : Fixture<Unit> {

    override fun defaultConfig() = Unit
}