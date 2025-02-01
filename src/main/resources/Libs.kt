import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.internal.GradleInternal
import org.gradle.internal.management.VersionCatalogBuilderInternal

internal val Project.libs: LibrariesForLibs
    get() {
        val versionCatalogBuilder = (gradle as GradleInternal).settings
            .dependencyResolutionManagement
            .dependenciesModelBuilders
            .find { it.name == "libs" }
            ?: error("Version catalog 'libs' not found!")

        val model = (versionCatalogBuilder as VersionCatalogBuilderInternal).build()
        return objects.newInstance(LibrariesForLibs::class.java, model)
    }
