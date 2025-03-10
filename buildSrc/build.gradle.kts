@file:Suppress("UnstableApiUsage")

plugins {
    `kotlin-dsl`
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter()
        }
    }
}