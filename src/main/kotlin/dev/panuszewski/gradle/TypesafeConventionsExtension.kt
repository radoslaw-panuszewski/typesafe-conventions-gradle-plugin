package dev.panuszewski.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

public abstract class TypesafeConventionsExtension(objects: ObjectFactory) {
    /**
     * (enabled by default)
     *
     * Enable or disable support for version catalog typesafe accessors in plugins block of a convention plugin.
     *
     * For example:
     * ```kotlin
     * // buildSrc/src/main/kotlin/some-convention.gradle.kts
     *
     * plugins {
     *     alias(libs.plugins.kotlin.jvm)
     * }
     * ```
     *
     * @since 0.5.0
     */
    public val accessorsInPluginsBlock: Property<Boolean> = objects.property<Boolean>().convention(true)

    /**
     * (enabled by default)
     *
     * Enable or disable auto dependency for every `alias(...)` plugin declaration in a convention plugin.
     *
     * Given the plugin declaration in convention plugin:
     * ```kotlin
     * // file: buildSrc/src/main/kotlin/some-convention.gradle.kts
     *
     * plugins {
     *     alias(libs.plugins.kotlin.jvm)
     * }
     * ```
     *
     * The following dependency will be added automatically:
     * ```kotlin
     * // file: buildSrc/build.gradle.kts
     *
     * dependencies {
     *     implementation(pluginMarker(libs.plugins.kotlin.jvm))
     * }
     * ```
     *
     * NOTE: it won't add dependencies for `id(...)` plugin declarations:
     * ```kotlin
     * plugins {
     *     // no dependency will be added for this, you should do it manually!
     *     id("org.jetbrains.kotlin.jvm")
     * }
     * ```
     *
     * @since 0.5.0
     */
    public val autoPluginDependencies: Property<Boolean> = objects.property<Boolean>().convention(true)
}