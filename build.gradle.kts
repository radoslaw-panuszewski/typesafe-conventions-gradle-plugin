plugins {
    `kotlin-dsl`
    `testing-convention`
    `plugin-publishing-convention`
}

kotlin {
    jvmToolchain(17)
    explicitApi()
}

dependencies {
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.junit.jupiter.params)
}