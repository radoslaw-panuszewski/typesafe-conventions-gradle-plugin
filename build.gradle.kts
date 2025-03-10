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
    implementation(libs.kotlin.gradle.plugin) { version { prefer(embeddedKotlinVersion) } }

    testImplementation(libs.kotest.assertions)
    testImplementation(libs.junit.jupiter.params)
}