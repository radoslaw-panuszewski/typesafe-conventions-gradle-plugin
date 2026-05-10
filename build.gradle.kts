plugins {
    `kotlin-dsl`
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
}

detekt {
    buildUponDefaultConfig = true
    config.from(files("detekt.yml"))
}

dependencies {
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.junit.jupiter.params)
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
    }
}
