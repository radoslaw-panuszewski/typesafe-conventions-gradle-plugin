# Typesafe Conventions Gradle Plugin

[![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/dev.panuszewski.typesafe-conventions?style=flat)](https://plugins.gradle.org/plugin/dev.panuszewski.typesafe-conventions)

A plugin that will bring type-safety to your convention plugins!

```diff
dependencies {
-    implementation(versionCatalogs.find("libs").get().findLibrary("kotlin-stdlib").get())
+    implementation(libs.kotlin.stdlib)
}
```

## Rationale

According to [Gradle docs](https://docs.gradle.org/8.12.1/userguide/sharing_build_logic_between_subprojects.html), it is recommended to place convention plugins inside the included build or `buildSrc` (which is also treated as an included build). In the ideal world, we would just copy the contents of our `build.gradle.kts` and put it inside `buildSrc/src/some-convention.gradle.kts` to be reused between subprojects. However, there are some serious limitations:
* the convention plugin can't use version catalog typesafe accessors (like `libs.kotlin.stdlib`)
* the buildscript of included build (e.g `buildSrc/build.gradle.kts`) doesn't have access to the version catalog of the main build
* there is no built-in way to convert plugin ID (like `org.jetbrains.kotlin.jvm`) to plugin dependency (like `org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.10`)

> [!NOTE]
> If Gradle fixes some of the issues mentioned above, the respective features will be removed from `typesafe-conventions`. Ideally, all the features will be removed, and this plugin will not be needed anymore ;) In that case, this README will point to Gradle docs with the replacements.

## Quickstart

### Prerequisites

* Gradle version is at least 8.4
* There is `gradle/*.versions.toml` file
* There is an included build for build logic (we will refer to it as `buildSrc`)
* At least one project within `buildSrc` has [precompiled script plugins](https://docs.gradle.org/8.12.1/userguide/implementing_gradle_plugins_precompiled.html) enabled (you can do this by applying the `kotlin-dsl` plugin)

### Usage

Apply `typesafe-conventions` in `buildSrc/settings.gradle.kts`:

> [!IMPORTANT]
> It's a settings plugin (not project plugin) so apply it in `settings.gradle.kts`!

```kotlin
plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.4.1"
}
```

Your project structure should be similar to the following:
```bash
.
├── gradle/
│   └── libs.versions.toml   # define 'kotlin-jvm' plugin and 'kotlin-stdlib' library here
├── settings.gradle.kts
├── build.gradle.kts
├── ...
└── buildSrc/
    ├── settings.gradle.kts  # apply 'typesafe-conventions' here
    ├── build.gradle.kts     # you can use 'pluginMarker(libs.plugin.kotlin.jvm)' here! 🚀
    └── src/
        └── main/
            └── kotlin/
                └── some-convention.gradle.kts  # you can use 'libs.kotlin.stdlib' here! 🚀
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

#### Named package

If you keep your convention plugins in a named package (for example in `buildSrc/src/main/kotlin/com/myapp/gradle`), you need to explicitly import the `libs` extension:
```diff
package com.myapp.gradle

+import libs

dependencies {
    implementation(libs.kotlin.stdlib)
}
```

### Version catalog in `buildSrc` buildscript

In plain Gradle, using version catalog in `buildSrc/build.gradle.kts` would require manually registering it in the `buildSrc/settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
```

After applying `typesafe-conventions`, you don't need the above configuration - it works out-of-the-box.

### Converting plugin ID to plugin dependency in `buildSrc` buildscript

In plain Gradle, you need to manually make up the coordinates of plugin's marker artifact:
```kotlin
dependencies {
    val plugin = libs.plugins.kotlin.jvm.get()
    implementation("${plugin.pluginId}:${plugin.pluginId}.gradle.plugin:${plugin.version}")
}
```

After applying `typesafe-conventions`, you can use `pluginMarker` helper method:
```kotlin
import dev.panuszewski.gradle.pluginMarker

dependencies {
    implementation(pluginMarker(libs.plugins.kotlin.jvm))
}
```

## Multi-project setup (custom included build)

As an alternative to `buildSrc`, you can use custom included build (typically named `build-logic`). The `typesafe-conventions` will fit nicely in this kind of setup.

As opposed to `buildSrc`, the included build can have multiple subprojects with convention plugins. For example, you can have something like this:
```bash
.
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts       # includeBuild("build-logic")
├── ...
└── build-logic/
    ├── settings.gradle.kts   # apply 'typesafe-conventions' here
    ├── ...
    ├── first-convention-plugin/
    │   └── build.gradle.kts  # apply 'kotlin-dsl' here
    └── second-convention-plugin/
        └── build.gradle.kts  # apply 'kotlin-dsl' here
```

> [!TIP]
> Why would you prefer `build-logic` over `buildSrc`? If your build contains a lot of projects, and those projects apply different combinations of convention plugins, placing every convention plugin in its own subproject can improve performance. It's because modifying the convention plugin code will only trigger reload of the subprojects that are actually using it. 

