# Typesafe Conventions Gradle Plugin

A plugin that will make your convention plugins looking almost like regular `*.gradle.kts` buildscripts!

## Rationale

According to [Gradle docs](https://docs.gradle.org/8.12.1/userguide/sharing_build_logic_between_subprojects.html), it is recommended to place convention plugins inside the included build or `buildSrc` (which is also treated as an included build). In the ideal world, we would just copy the contents of our `build.gradle.kts` and put it inside `buildSrc/src/some-convention.gradle.kts` to be reused between subprojects. However, there are some serious limitations:
* the convention plugin can't use version catalog typesafe accessors (like `libs.kotlin.stdlib`)
* the buildscript of included build (e.g `buildSrc/build.gradle.kts`) also doesn't have access to the version catalog of the main build
* the convention plugin needs to have the plugins on the classpath before applying them, and there is no built-in way to convert plugin ID (like `org.jetbrains.kotlin.jvm`) to plugin dependency (like `org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.10`)

This plugin will fix the above issues for your `buildSrc` 😉

## Usage

Apply `typesafe-conventions` in `buildSrc/settings.gradle.kts`:
```kotlin
plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.1.0"
}
```

Additionally, apply `kotlin-dsl` in `buildSrc/build.gradle.kts` to to enable [precompiled script plugins](https://docs.gradle.org/8.12.1/userguide/implementing_gradle_plugins_precompiled.html):
```kotlin
plugins {
    `kotlin-dsl`
}
```

Your project structure should be similar to the following:
```bash
.
├── settings.gradle.kts
├── build.gradle.kts
├── ...
└── buildSrc/
    ├── settings.gradle.kts # <-- apply `typesafe-conventions` here
    ├── build.gradle.kts # <-- apply `kotlin-dsl` here
    └── src/
        └── main/
            └── kotlin/
                └── some-convention.gradle.kts # <-- here you can use typesafe accessors!
```

## Features

### Version catalog in convention plugins

In plain Gradle, applying dependency from version catalog in a convention plugin would look like this:
```kotlin
dependencies {
    implementation(versionCatalogs.find("libs").get().findLibrary("kotlin-stdlib").get())
}
```
After applying `typesafe-conventions`, you can benefit from type-safe syntax:
```kotlin
dependencies {
    implementation(libs.kotlin.stdlib)
}
```

### Version catalog in `buildSrc` buildscript

Without `typesafe-conventions`, applying dependency from version catalog in `buildSrc/build.gradle.kts` would require manually creating the version catalog in `buildSrc/settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
```

After applying `typesafe-conventions`, you don't need the above configuration.

### Converting plugin ID to plugin dependency in `buildSrc` buildscript

In plain Gradle, you need to manually concatenate the coordinates of plugin's marker artifact:
```kotlin
dependencies {
    val plugin = libs.plugins.kotlin.jvm.get()
    implementation("${plugin.pluginId}:${plugin.pluginId}.gradle.plugin:${plugin.version}")
}
```

After applying `typesafe-conventions`, you can use `pluginMarker` helper method:
```kotlin
dependencies {
    implementation(pluginMarker(libs.plugins.kotlin.jvm))
}
```

## Included builds

As an alternative to `buildSrc`, you can use included build (typically named `build-logic`). The `typesafe-conventions` will fit nicely in this kind of setup.

> [!WARNING]
> Make sure that your included build is nested within the main build!

```bash
.
├── build.gradle.kts
├── settings.gradle.kts # <-- includeBuild("build-logic")
├── ...
└── build-logic/
    ├── build.gradle.kts # <-- apply `kotlin-dsl` here
    ├── settings.gradle.kts # <-- apply `typesafe-conventions` here
    └── src/
        └── main/
            └── kotlin/
                └── some-convention.gradle.kts # <-- here you can use typesafe accessors!
```

As opposed to `buildSrc`, the included build can have multiple subprojects with convention plugins. For example, you can have something like this:
```bash
.
├── build.gradle.kts
├── settings.gradle.kts # <-- includeBuild("build-logic")
├── ...
└── build-logic/
    ├── settings.gradle.kts # <-- apply `typesafe-conventions` here
    ├── ...
    ├── first-convention-plugin/
    │   └── build.gradle.kts # <-- apply `kotlin-dsl` here
    └── second-convention-plugin/
        └── build.gradle.kts # <-- apply `kotlin-dsl` here
```

Why would you prefer `build-logic` over `buildSrc`? If your build contains a lot of projects, and those projects apply different combinations of convention plugins, placing every convention plugin in its own subproject can improve performance. It's because modifying the convention plugin code will only trigger reload of the subprojects that are actually using it. 

