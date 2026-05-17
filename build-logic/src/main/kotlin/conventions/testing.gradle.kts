@file:Suppress("UnstableApiUsage")

package conventions

import libs

plugins {
    java
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
        withType<JvmTestSuite>().configureEach {
            useJUnitJupiter()

            dependencies {
                implementation(project())
                implementation(libs.kotest.assertions)
                implementation(libs.junit.jupiter.params)
            }

            targets.all {
                tasks.check {
                    dependsOn(testTask)
                }
            }
        }

        register<JvmTestSuite>("functionalTest") {
            targets.all {
                dependencies {
                    implementation(gradleTestKit())
                }

                testTask {
                    dependsOn("publishToMavenLocal")

                    environment["PROJECT_VERSION"] = project.version

                    environment["GRADLE_VERSION_TO_TEST"] = findProperty("gradleVersionToTest")
                        ?: findProperty("gVTT")
                        ?: System.getenv("GRADLE_VERSION_TO_TEST")
                        ?: gradle.gradleVersion
                }
            }
        }
    }
}
