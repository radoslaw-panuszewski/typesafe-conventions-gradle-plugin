@file:Suppress("UnstableApiUsage")

plugins {
    `jvm-test-suite`
    alias(libs.plugins.test.logger)
}

testlogger {
    showStandardStreams = true
    showPassedStandardStreams = false
    showSkippedStandardStreams = false
    showFailedStandardStreams = true
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter()
        }
    }
}

tasks {
    named<Test>("test") {
        dependsOn("publishToMavenLocal")

        environment["PROJECT_VERSION"] = project.version

        environment["GRADLE_VERSION_TO_TEST"] = findProperty("gradleVersionToTest")
            ?: findProperty("gVTT")
                ?: System.getenv("GRADLE_VERSION_TO_TEST")
                ?: gradle.gradleVersion
    }
}
