plugins {
    `kotlin-dsl`
    `maven-publish`
    alias(libs.plugins.axion.release)
}

scmVersion {
    unshallowRepoOnCI = true
}

group = "dev.panuszewski"
version = scmVersion.version

java {
    withSourcesJar()
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    plugins {
        create("typesafeConventions") {
            id = "dev.panuszewski.typesafe-conventions"
            implementationClass = "dev.panuszewski.gradle.TypesafeConventionsSettingsPlugin"
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}