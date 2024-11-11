plugins {
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm") version "2.0.20"
}

kotlin {
    jvmToolchain(21)
}

gradlePlugin {
    plugins {
        create("plugin") {
            id = "dev.panuszewski.catalogs-everywhere"
            implementationClass = "dev.panuszewski.gradle.CatalogsEverywhereGradlePlugin"
        }
    }
}

dependencies {
    implementation(gradleKotlinDsl())
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api")
}

tasks {
    test {
        useJUnitPlatform()
    }
}