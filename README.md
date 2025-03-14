# Typesafe Conventions Gradle Plugin

[![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/dev.panuszewski.typesafe-conventions?style=flat)](https://plugins.gradle.org/plugin/dev.panuszewski.typesafe-conventions)

A plugin that will bring type-safety to your convention plugins!

```diff
plugins {
-    id("org.jetbrains.kotlin.jvm")
+    alias(libs.plugins.kotlin.jvm)
}

dependencies {
-    implementation(versionCatalogs.find("libs").get().findLibrary("kotlin-stdlib").get())
+    implementation(libs.kotlin.stdlib)
}
```

# Rationale

According to [Gradle docs](https://docs.gradle.org/8.12.1/userguide/sharing_build_logic_between_subprojects.html), it is recommended to place convention plugins inside the included build or `buildSrc` (which is also treated as an included build). In the ideal world, we would just copy the contents of our `build.gradle.kts` and put it inside `buildSrc/src/some-convention.gradle.kts` to be reused between subprojects. However, there are some serious limitations:
* the convention plugin can't use version catalog typesafe accessors (like `libs.kotlin.stdlib` or `libs.plugins.kotlin.jvm`)
* the buildscript of included build (e.g `buildSrc/build.gradle.kts`) doesn't have access to the version catalog of the main build
* if convention plugin wants to apply an external plugin, the plugin dependency must be manually added to `buildSrc`
* there is no built-in way to convert plugin ID (like `org.jetbrains.kotlin.jvm`) to plugin dependency (like `org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.10`)

> [!NOTE]
> If Gradle fixes some of the issues mentioned above, the respective features will be removed from `typesafe-conventions`. Ideally, all the features will be removed, and this plugin will not be needed anymore ;) In that case, this README will point to Gradle docs with the replacements.

# Quickstart

If you prefer watching over reading, check out this [cool video](https://www.youtube.com/watch?v=9BhtoNxgPks) from Duncan McGregor 😉

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
    id("dev.panuszewski.typesafe-conventions") version "0.5.0"
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

### Configuration

The following snippet presents all possible configuration options with their default values:
```kotlin
typesafeConventions {
    // enable or disable support for version catalog typesafe accessors in plugins block of a convention plugin
    accessorsInPluginsBlock = true

    // enable or disable auto dependency for every alias(...) plugin declaration in a convention plugin
    // set it to 'false' if you prefer to add plugin marker dependencies manually (you can use the pluginMarker helper method for that) 
    autoPluginDependencies = true
}
```

In Gradle < 8.8, you should use the following syntax instead:
```kotlin
import dev.panuszewski.gradle.TypesafeConventionsExtension

configure<TypesafeConventionsExtension> {
    accessorsInPluginsBlock = true
    // ...
}
```

# Features

## Version catalog in convention plugins

### ⏩ TL;DR

```diff
dependencies {
-    implementation(versionCatalogs.find("libs").get().findLibrary("kotlin-stdlib").get())
+    implementation(libs.kotlin.stdlib)
}
```

### 🔍 Details

In plain Gradle, applying dependency from version catalog in a convention plugin would look like this:
```kotlin
dependencies {
    implementation(versionCatalogs.find("libs").get().findLibrary("kotlin-stdlib").get())
}
```

With `typesafe-conventions`, you can benefit from type-safe syntax:
```kotlin
dependencies {
    implementation(libs.kotlin.stdlib)
}
```

### Named package

If you keep your convention plugins in a named package (for example in `buildSrc/src/main/kotlin/com/myapp/gradle`), you need to explicitly import the `libs` extension:
```diff
package com.myapp.gradle

+import libs

dependencies {
    implementation(libs.kotlin.stdlib)
}
```

## Version catalog in `plugins {}` block of a convention plugin

### ⏩ TL;DR

buildSrc/src/main/kotlin/some-convention.gradle.kts:
```diff
plugins {
-    id("org.jetbrains.kotlin.jvm")
+    alias(libs.plugins.kotlin.jvm)
}
```

buildSrc/build.gradle.kts:
```diff
-dependencies {
-    implementation(pluginMarker(libs.plugins.kotlin.jvm))
-}
-
-fun pluginMarker(provider: Provider<PluginDependency>): String {
-    val plugin = provider.get()
-    return "${plugin.pluginId}:${plugin.pluginId}.gradle.plugin:${plugin.version}"
-}
```

### 🔍 Details

Let's assume you have the following plugin declared in your `libs.versions.toml`:
```toml
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version = "2.1.10" }
```

In plain Gradle, to apply external plugin from your convention plugin, you would need to add it as dependency to your `buildSrc/build.gradle.kts`:
```kotlin
dependencies {
    implementation(pluginMarker(libs.plugins.kotlin.jvm))
}

/**
 * This is a custom helper method to convert plugin ID into plugin marker artifact.
 * Alternatively, you could add a dependency to "org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.10",
 * but you need to know the exact coordinates of the plugin artifact (which can be arbitrary).
 */
fun pluginMarker(provider: Provider<PluginDependency>): String {
    val plugin = provider.get()
    return "${plugin.pluginId}:${plugin.pluginId}.gradle.plugin:${plugin.version}"
}
```

And then apply it in your convention plugin via `id(...)` syntax:
```kotlin
plugins {
    id("org.jetbrains.kotlin.jvm")
}
```

With `typesafe-conventions`, you can just apply the plugin via `alias(...)` syntax in your convention plugin:
```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
}
```

The plugin dependency will be added automatically to the project that hosts your convention plugin. In our case it's just the root project of `buildSrc`.

### Manual plugin dependencies

If you prefer to add the plugin dependencies manually, you can opt out from the auto dependencies feature in your `buildSrc/settings.gradle.kts`:
```kotlin
typesafeConventions {
    autoPluginDependencies = false
}
```

And use the `pluginMarker` helper method in `buildSrc/build.gradle.kts`:
```kotlin
import dev.panuszewski.gradle.pluginMarker

dependencies {
    implementation(pluginMarker(libs.plugins.kotlin.jvm))
}
```

### Overriding versions of auto plugin dependencies

The auto dependencies will have the weakest version constraint (`prefer`) so you can easily override the version by simply declaring the dependency.

> [!WARNING]
> This method works only for plugin marker artifacts, not the actual artifacts that contain plugin code!
> For example, it works for `org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin`,
> but not for `org.jetbrains.kotlin:kotlin-gradle-plugin`

You may want to do it when you determine the version dynamically:
```kotlin
import dev.panuszewski.gradle.pluginMarker

dependencies {
    val kotlinVersion = resolveKotlinVersionFromSpringBom()
    
    // hardcode the coordinates...
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:$kotlinVersion")
    
    // ...or use version catalog accessor and set stronger constraint
    // (the accessor comes with 'require' constraint by default, so you need to use 'strictly')
    implementation(pluginMarker(libs.plugins.kotlin.jvm)) { version { strictly(kotlinVersion) } }
}
```

## Version catalog in `buildSrc` buildscript

### ⏩ TL;DR

buildSrc/settings.gradle.kts:
```diff
-dependencyResolutionManagement {
-    versionCatalogs {
-        create("libs") {
-            from(files("../gradle/libs.versions.toml"))
-        }
-    }
-}
```

### 🔍 Details

In plain Gradle, using version catalog in `buildSrc/build.gradle.kts` would require manually registering it in the `buildSrc/settings.gradle.kts`. After applying `typesafe-conventions`, you don't need the above configuration - it works out-of-the-box.

# Multi-project setup (custom included build)

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

