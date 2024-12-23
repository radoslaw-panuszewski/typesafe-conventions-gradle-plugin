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

java {
    withSourcesJar()
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    plugins {
        create("projectPlugin") {
            id = "dev.panuszewski.typesafe-conventions"
            implementationClass = "dev.panuszewski.gradle.TypesafeConventionsPlugin"
        }
        create("settingsPlugin") {
            id = "dev.panuszewski.typesafe-conventions-settings"
            implementationClass = "dev.panuszewski.gradle.TypesafeConventionsSettingsPlugin"
        }
    }
}

dependencies {
    compileOnly(gradleKotlinDsl())
}

tasks {
    test {
        useJUnitPlatform()
    }
}