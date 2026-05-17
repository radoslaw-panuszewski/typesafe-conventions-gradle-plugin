<h1>
<p align="center">
    <a href="https://plugins.gradle.org/plugin/dev.panuszewski.typesafe-conventions"><img src="https://img.shields.io/gradle-plugin-portal/v/dev.panuszewski.typesafe-conventions?style=flat" /></a>
    <br />
    Typesafe Conventions Gradle Plugin
    </h1>
    <p align="center">
        A plugin that will bring type-safety to your convention plugins!
        <br />
        <a href="#rationale">Rationale</a>
        ·
        <a href="#quickstart">Quickstart</a>
        ·
        <a href="#features">Features</a>
        ·
        <a href="#less-common-use-cases">Less common use cases</a>
        ·
        <a href="#troubleshooting">Troubleshooting</a>
    </p>
</p>

```diff
plugins {
-   id("org.jetbrains.kotlin.jvm")
+   alias(libs.plugins.kotlin.jvm)

-   id("conventions.kotlin")
+   alias(conventions.plugins.kotlin)
}

dependencies {
-   implementation(versionCatalogs.find("libs").get().findLibrary("kotlin-stdlib").get())
+   implementation(libs.kotlin.stdlib)
}
```

# Rationale

According to the [Gradle docs](https://docs.gradle.org/current/userguide/best_practices_structuring_builds.html#favor_composite_builds), it is recommended to place convention plugins inside the `build-logic` included build. In an ideal world, we would just copy some part of `build.gradle.kts` and put it inside a convention plugin. However, there are some serious limitations:
* the convention plugin can't use version catalog typesafe accessors
* the buildscript of `build-logic` doesn't have access to the version catalog of the main build
* if a convention plugin wants to apply an external plugin, the plugin dependency must be manually added
* there is no built-in way to convert plugin ID to plugin dependency

The `typesafe-conventions` plugin aims to solve these problems.

# Quickstart

If you prefer watching over reading, check out this [cool video](https://www.youtube.com/watch?v=9BhtoNxgPks) from Duncan McGregor 😉

### Prerequisites

* Gradle 8.8+
* JDK 17+
* Either local or imported version catalog is used
* There is an included build for build logic (we will refer to it as `build-logic`)
* At least one project within `build-logic` has [precompiled script plugins](https://docs.gradle.org/8.12.1/userguide/implementing_gradle_plugins_precompiled.html) enabled (you can do this by applying the `kotlin-dsl` plugin)

### Usage

Apply the `typesafe-conventions` plugin in `build-logic/settings.gradle.kts`:

> [!IMPORTANT]
> It's a settings plugin (not a project plugin) so apply it in `settings.gradle.kts`!

```kotlin
plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.11.0"
}
```

Your project structure should be similar to the following:
```bash
.
├── gradle/
│   └── libs.versions.toml
├── settings.gradle.kts
├── build.gradle.kts
├── ...
└── build-logic/
    ├── settings.gradle.kts  # apply 'typesafe-conventions' here
    ├── build.gradle.kts
    └── src/
        └── main/
            └── kotlin/
                └── some-convention.gradle.kts
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

    // whether to allow plugin usage for a top-level build
    // set it to 'true' only if you know what you're doing!
    allowTopLevelBuild = false

    conventionCatalog {
        // enable or disable support for convention catalog
        enabled = true

        // name of the version catalog that will contain convention plugins
        catalogName = "conventions"

        // whether to skip package names in convention catalog entries
        ignorePackageNames = false
    }
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

* [Version catalog accessors for libraries](#version-catalog-accessors-for-libraries)
* [Version catalog accessors for plugins](#version-catalog-accessors-for-plugins)
* [Auto-import of a version catalog from the parent build](#auto-import-of-a-version-catalog-from-the-parent-build)
* [Convention catalog](#convention-catalog)

## Version catalog accessors for libraries

build-logic/src/main/kotlin/some-convention.gradle.kts:
```diff
dependencies {
-    implementation(versionCatalogs.find("libs").get().findLibrary("kotlin-stdlib").get())
+    implementation(libs.kotlin.stdlib)
}
```

<details>
<summary>Details</summary>

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

If you keep your convention plugins in a named package (for example in `build-logic/src/main/kotlin/com/myapp/gradle`), you need to explicitly import the `libs` extension:
```diff
package com.myapp.gradle

+import libs

dependencies {
    implementation(libs.kotlin.stdlib)
}
```
</details>

## Version catalog accessors for plugins

build-logic/src/main/kotlin/some-convention.gradle.kts:
```diff
plugins {
-    id("org.jetbrains.kotlin.jvm")
+    alias(libs.plugins.kotlin.jvm)
}
```

build-logic/build.gradle.kts:
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

> [!TIP]
> The plugin dependency is automatically added to the project that hosts your convention plugin.

<details>
<summary>Details</summary>
Let's assume you have the following plugin declared in your `libs.versions.toml`:
```toml
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version = "2.1.10" }
```

In plain Gradle, to apply external plugin from your convention plugin, you would need to add it as dependency to your `build-logic/build.gradle.kts`:
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

### Manual plugin dependencies

If you prefer to add the plugin dependencies manually, you can opt out from the auto dependencies feature in your `build-logic/settings.gradle.kts`:
```kotlin
typesafeConventions {
    autoPluginDependencies = false
}
```

And use the `pluginMarker` helper method in `build-logic/build.gradle.kts`:
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

</details>

## Auto-import of a version catalog from the parent build

build-logic/settings.gradle.kts:
```diff
-dependencyResolutionManagement {
-    versionCatalogs {
-        create("libs") {
-            from(files("../gradle/libs.versions.toml"))
-        }
-    }
-}
```

<details>
<summary>Details</summary>

In plain Gradle, using version catalog in `build-logic/build.gradle.kts` would require manually registering it in the `build-logic/settings.gradle.kts`. 

After applying `typesafe-conventions`, you don't need the above configuration — it works out-of-the-box.

</details>

## Convention catalog

> [!WARNING]
> This feature is not available for `buildSrc`! If you want to use it, please migrate to `build-logic`.
> See [Gradle Best Practices](https://docs.gradle.org/current/userguide/best_practices_structuring_builds.html#favor_composite_builds) for more details.

build.gradle.kts:
```diff
plugins {
-   id("some-convention")
+   alias(conventions.plugins.some.convention)
}
```

build-logic/src/main/kotlin/some-convention.gradle.kts:
```diff
plugins {
-   id("another-convention")
+   alias(conventions.plugins.another.convention)
}
```

build-logic/src/main/kotlin/another-convention.gradle.kts:
```kotlin
// the alias used above is generated due to existence of this file
```

<details>
<summary>Details</summary>

In plain Gradle, you must provide a convention plugin name via raw String:
```kotlin
plugins {
    id("some-convention")
}
```

The `typesafe-conventions` plugin automatically creates the _convention catalog_ for you. It is a special version catalog that contains references to all your convention plugins.

You can use it to apply your convention plugin in a type-safe way:
```kotlin
plugins {
    alias(conventions.plugins.some.convention)
}
```

### Custom name for convention catalog

If you don't like the default name `conventions`, you can change it:
```kotlin
typesafeConventions {
    conventionCatalog {
        catalogName = "buildlogic"
    }
}
```

```kotlin
plugins {
    alias(buildlogic.plugins.some.convention)
}
```

### Convention plugins in named packages

If your convention plugin is placed in a named package, the package name will be included in the resulting alias in the convention catalog:

build-logic/src/main/kotlin/com/example/some-convention.gradle.kts:
```kotlin
package com.example

// some config...
```

build.gradle.kts:
```kotlin
plugins {
    alias(conventions.plugins.com.example.some.convention)
}
```

You can disable this behavior:
```kotlin
typesafeConventions {
    conventionCatalog {
        ignorePackageNames = true
    }
}
```

WARNING: When this property is set to `true`, every convention plugin name must be unique!

### Avoiding conflicts with built-in plugins

It's pretty easy to create a convention plugin with the same name as a built-in plugin.

For example, it may be tempting to create `publishing.gradle.kts` convention plugin for shared publishing configuration. However, the resulting ID of such plugin would be just `publishing`, which is the same as the built-in Gradle [`publishing`](https://github.com/gradle/gradle/blob/master/platforms/software/publish/src/main/java/org/gradle/api/publish/plugins/PublishingPlugin.java) plugin.

To avoid such conflicts, you can put your `publishing.gradle.kts` into a named package (e.g `conventions`):

```kotlin
// build-logic/src/main/kotlin/conventions/publishing.gradle.kts
package conventions
```

To keep the plugin alias nice and short, you can also enable ignoring package names:
```kotlin
typesafeConventions {
    conventionCatalog {
        ignorePackageNames = true
    }
}
```

This way, you can apply your plugins like this:
```kotlin
plugins {
    publishing // the built-in plugin
    alias(conventions.plugins.publishing) // your convention plugin 
}
```

</details>

# Less common use cases

## Custom version catalogs 

It's perfectly OK to use other version catalogs than `libs` as `typesafe-conventions` supports them out-of-the-box!

### Local catalogs

Create a file like `gradle/custom.versions.toml` with the contents similar to `gradle/libs.versions.toml`.

Remember that you must manually register it in `settings.gradle.kts`! (that's how Gradle works, nothing to do with `typesafe-conventions`):
```kotlin
dependencyResolutionManagement {
    versionCatalogs {
        create("custom") {
            from(files("gradle/custom.versions.toml"))
        }
    }
}
```

### Imported catalogs

> [!WARNING]
> This feature is not available for builds included within the `pluginManagement { ... }` block!
> 
> (see [known limitations](#known-limitations))

Import the catalog in your `settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    versionCatalogs {
        create("mn") {
            from("io.micronaut.platform:micronaut-platform:4.8.2")
        }
    }
}
```

In the example above, we import the version catalog provided by Micronaut.

#### Known limitations

Gradle allows you to include builds in your `settings.gradle.kts` like this:
```kotlin
pluginManagement {
    includeBuild("build-logic")
}
```

This instructs the `build-logic` to evaluate before settings of the main build and thus allows you to write convention plugins to be applied in `settings.gradle.kts` of the main build. 

Unfortunately, the above config makes it impossible for the included build to inherit imported version catalogs of the main build (as those become available after the main build's settings are evaluated). Additionally, it's impossible to rebuild the original build hierarchy for early evaluated builds before they were flattened by Gradle.

In a rare case when you need to provide a settings convention plugin, please extract it to a separate included build and refrain from using typesafe-conventions in that build.

Most of the time, though, it is perfectly OK to migrate your `build-logic` to a regular included build:
```diff
-pluginManagement {
    includeBuild("build-logic")
-}
```

## Multiple projects in `build-logic` 

Your `build-logic` can have multiple subprojects with convention plugins. For example, you can have something like this:
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

## Top-level build

In most cases, you should apply `typesafe-conventions` to either included build or `build-logic`, because that's 
where convention plugins are typically stored and the included build will "inherit" version catalogs from 
the main build.

By default, applying `typesafe-conventions` to a top-level build is not allowed, but you can change this default in your `settings.gradle.kts`:
```kotlin
typesafeConventions { 
    allowTopLevelBuild = true 
}
```
> [!WARNING]
> Allow top-level build only if you know what you're doing!

If you apply `typesafe-conventions` to a top-level build, be aware that your convention plugins will be
compiled against typesafe accessors generated for version catalogs (TOML files) your local build has access to.
If you publish your convention plugins to remote repo and use them in another build, make sure the version
catalogs in that another build are **exactly** the same as in the original one. Otherwise, you will end up if
referencing catalog entries that are not existing!

A valid use case is when you publish your convention plugins together with the version catalog (with the same 
version) and the catalog has entry to self-reference this version. This way, you can assure the version 
catalog and convention plugins are always in sync.

# Troubleshooting

## Linters & static analysis tools

If you use any linters or static analysis tools, like [ktlint-gradle](https://github.com/JLLeitschuh/ktlint-gradle), [kotlinter](https://github.com/jeremymailen/kotlinter-gradle) or [detekt](https://github.com/detekt/detekt), Gradle may complain about implicit task dependencies:
> Task ':build-logic:runKtlintCheckOverMainSourceSet' uses this output of task ':build-logic:generateEntrypointForLibs' without declaring an explicit or implicit dependency. This can lead to incorrect results being produced, depending on what order the tasks are executed.

To fix this issue, you should exclude the code generated by the `typesafe-conventions` from static analysis. All code generated by this plugin is placed under `build/generated-sources/typesafe-conventions` directory.

> [!TIP]
> It may be a good idea to exclude all generated code from static analysis, not only the classes generated by `typesafe-conventions`! 

For [ktlint-gradle](https://github.com/JLLeitschuh/ktlint-gradle), it may look like this:
```kotlin
ktlint {
    filter {
        exclude { it.file.path.contains("build/generated-sources/typesafe-conventions") }
    }
}
```
