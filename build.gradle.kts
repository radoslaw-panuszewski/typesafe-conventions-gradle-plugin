plugins {
    `kotlin-dsl`
    alias(libs.plugins.plugin.publish)
    alias(libs.plugins.axion.release)
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

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
    }

    publishPlugins {
        notCompatibleWithConfigurationCache("Uses Task.project at execution time")
    }
}