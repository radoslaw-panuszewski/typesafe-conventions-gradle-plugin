plugins {
    `kotlin-dsl`
    `testing-convention`
    `publishing-convention`
    alias(libs.plugins.kotlinter)
}

kotlin {
    jvmToolchain(17)
    explicitApi()
    compilerOptions {
        freeCompilerArgs.add("-Werror")
    }
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
