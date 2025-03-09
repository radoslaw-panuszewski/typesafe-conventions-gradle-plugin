plugins {
    `kotlin-dsl`
    alias(libs.plugins.plugin.publish)
    alias(libs.plugins.axion.release)
    alias(libs.plugins.test.logger)
}

scmVersion {
    unshallowRepoOnCI = true
}

group = "dev.panuszewski"
version = scmVersion.version

kotlin {
    jvmToolchain(17)
    explicitApi()
}

gradlePlugin {
    website = "https://github.com/radoslaw-panuszewski/typesafe-conventions-gradle-plugin"
    vcsUrl = "https://github.com/radoslaw-panuszewski/typesafe-conventions-gradle-plugin"

    plugins {
        create("typesafeConventions") {
            id = "dev.panuszewski.typesafe-conventions"
            implementationClass = "dev.panuszewski.gradle.TypesafeConventionsPlugin"
            displayName = "Typesafe Conventions Plugin"
            description = "Gradle plugin providing typesafe accessors for convention plugins"
            tags = listOf("build-logic", "buildSrc")
        }
    }
}

testlogger {
    showStandardStreams = true
    showPassedStandardStreams = false
    showSkippedStandardStreams = false
    showFailedStandardStreams = true
}

dependencies {
    implementation(libs.kotlin.gradle.plugin) { version { prefer(embeddedKotlinVersion) } }
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.bundles.junit)
}

tasks {
    test {
        useJUnitPlatform()
        dependsOn(publishToMavenLocal)

        environment["PROJECT_VERSION"] = project.version

        environment["GRADLE_VERSION_TO_TEST"] = findProperty("gradleVersionToTest")
            ?: findProperty("gVTT")
            ?: System.getenv("GRADLE_VERSION_TO_TEST")
            ?: gradle.gradleVersion
    }

    publishPlugins {
        notCompatibleWithConfigurationCache("Uses Task.project at execution time")
    }
}