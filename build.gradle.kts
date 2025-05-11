plugins {
    `kotlin-dsl`
    `testing-convention`
    `publishing-convention`
}

kotlin {
    jvmToolchain(17)
    explicitApi()
}

dependencies {
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.junit.jupiter.params)
}