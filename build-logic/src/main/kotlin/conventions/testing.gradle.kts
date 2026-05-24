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
            }

            targets.all {
                tasks.check {
                    dependsOn(testTask)
                }
            }
        }

        register<JvmTestSuite>("functionalTest") {
            targets.all {
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

configurations {
    named("functionalTestCompileClasspath") {
        extendsFrom(testCompileClasspath)
    }
    named("functionalTestRuntimeClasspath") {
        extendsFrom(testRuntimeClasspath)
    }
}
