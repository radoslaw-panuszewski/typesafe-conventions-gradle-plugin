import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.catalog.parser.TomlCatalogFileParser
import org.gradle.internal.management.VersionCatalogBuilderInternal
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.support.serviceOf

internal val Project.libs: LibrariesForLibs
    get() {
        val versionCatalogBuilder = (gradle as GradleInternal).settings
            .dependencyResolutionManagement
            .dependenciesModelBuilders
            .get(0)
        val model = (versionCatalogBuilder as VersionCatalogBuilderInternal).build()
        return objects.newInstance(LibrariesForLibs::class, model)
    }