package dev.panuszewski.gradle.framework

typealias FileConfigurator = AppendableFile.() -> Any

typealias BuildConfigurator = GradleSpec.(GradleBuild.() -> Unit) -> Unit
