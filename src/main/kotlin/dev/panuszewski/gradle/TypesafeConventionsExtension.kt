package dev.panuszewski.gradle

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject
import kotlin.DeprecationLevel.WARNING

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
    @Deprecated(
        message = "This property will be removed in 1.0.0 (so it will be always enabled). " +
            "If you experience any problems with accessors in plugins block, please report an issue: " +
            "https://github.com/radoslaw-panuszewski/typesafe-conventions-gradle-plugin/issues",
        level = WARNING
    )
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

    /**
     * (disabled by default)
     *
     * Whether to allow plugin usage for a top-level build.
     * Set it to `true` only if you know what you're doing!
     *
     * @since 0.6.0
     */
    public val allowTopLevelBuild: Property<Boolean> = objects.property<Boolean>().convention(false)

    public val conventionCatalog: ConventionCatalogExtension = objects.newInstance(ConventionCatalogExtension::class.java)

    public fun conventionCatalog(action: Action<ConventionCatalogExtension>) {
        action.execute(conventionCatalog)
    }
}

public abstract class ConventionCatalogExtension @Inject constructor(objects: ObjectFactory) {
    /**
     * (default = "conventions")
     *
     * Name of the version catalog that will contain convention plugins.
     */
    public val catalogName: Property<String> = objects.property<String>().convention("conventions")
}
