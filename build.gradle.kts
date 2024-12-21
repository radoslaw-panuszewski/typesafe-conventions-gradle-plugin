plugins {
    `java-gradle-plugin`
    `maven-publish`
    `kotlin-dsl`
    alias(libs.plugins.axion.release)
}

scmVersion {
    unshallowRepoOnCI = true
}

group = "dev.panuszewski"
version = scmVersion.version

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    plugins {
        create("plugin") {
            id = "dev.panuszewski.typesafe-conventions"
            implementationClass = "dev.panuszewski.gradle.TypesafeConventionsPlugin"
        }
    }
}

dependencies {
    implementation(gradleKotlinDsl())
    implementation(libs.kotlin.gradle.plugin.api)
}

tasks {
    test {
        useJUnitPlatform()
    }
}