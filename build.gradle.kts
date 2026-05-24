plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version embeddedKotlinVersion
    alias(conventions.plugins.testing)
    alias(conventions.plugins.publishing)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.detekt)
}

kotlin {
    jvmToolchain(17)
    explicitApi()
    compilerOptions {
        freeCompilerArgs.add("-Werror")
    }
    target.compilations.named("functionalTest") {
        associateWith(target.compilations.getByName("test"))
    }
}

detekt {
    buildUponDefaultConfig = true
    config.from(files("detekt.yml"))
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
    }
}

dependencies {
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.junit.jupiter.params)

    functionalTestImplementation(gradleTestKit())
    functionalTestImplementation(libs.fuel)
    functionalTestImplementation(libs.kotlinx.serialization)
}
