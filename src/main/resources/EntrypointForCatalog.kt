import org.gradle.accessors.dm.LibrariesForCatalog
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

internal val Project.catalog: LibrariesForCatalog
    get() = extensions.getByType()
