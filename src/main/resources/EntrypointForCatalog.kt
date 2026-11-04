import org.gradle.accessors.dm.LibrariesFor{{Name}}
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectStreamClass

internal val Project.{{name}}: LibrariesFor{{Name}}
    get() = extensions.findByType() ?: detachedCatalog()

private fun Project.detachedCatalog(): LibrariesFor{{Name}} =
    objects.newInstance(
        LibrariesFor{{Name}}::class.java,
        detachedCatalogModel(),
    )

private fun detachedCatalogModel(): Any {
    val modelStream = LibrariesFor{{Name}}::class.java
        .getResourceAsStream(DETACHED_CATALOG_MODEL_RESOURCE)
        ?: error("Detached {{name}} model resource '$DETACHED_CATALOG_MODEL_RESOURCE' not found on the classpath")

    return {{Name}}ModelObjectInputStream(modelStream).use(ObjectInputStream::readObject)
}

private class {{Name}}ModelObjectInputStream(stream: InputStream) : ObjectInputStream(stream) {

    override fun resolveClass(desc: ObjectStreamClass): Class<*> =
        try {
            // Gradle runtime classloader, can access internal Gradle dependencies (like relocated Guava)
            Class.forName(desc.name, false, Project::class.java.classLoader)
        } catch (e: ClassNotFoundException) {
            // Current classloader, can access DefaultVersionCatalog
            // Normally it would delegate to its parent, but it's blocked by Gradle's FilteringClassLoader
            Class.forName(desc.name, false, javaClass.classLoader)
        }
}

private const val DETACHED_CATALOG_MODEL_RESOURCE = "/typesafe-conventions/{{name}}.ser"
