import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.PluginDependenciesSpecScope

/**
 * This extension property will never be actually executed, so it's safe to just throw [NotImplementedError].
 * It's only needed to prevent IntelliJ from reporting compile errors in the editor.
 */
internal val PluginDependenciesSpecScope.libs: LibrariesForLibs
    get() = throw NotImplementedError("Will never be executed")
