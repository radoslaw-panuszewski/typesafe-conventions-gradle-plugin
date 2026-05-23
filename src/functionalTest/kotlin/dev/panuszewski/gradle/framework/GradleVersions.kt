package dev.panuszewski.gradle.framework

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.gradle.util.GradleVersion

object GradleVersions {
    val GRADLE_VERSION_TO_TEST = resolveGradleVersion(System.getenv("GRADLE_VERSION_TO_TEST"))

    private fun resolveGradleVersion(versionOrAlias: String): GradleVersion {
        val version = when (versionOrAlias) {
            "release-milestone" -> MilestoneResolver.resolve()
            else -> versionOrAlias
        }
        return GradleVersion.version(version)
    }
}

private object MilestoneResolver {
    private val json = Json { ignoreUnknownKeys = true }

    fun resolve(): String =
        "https://services.gradle.org/versions/milestone"
            .httpGet()
            .responseString()
            .third
            .map { json.decodeFromString<Milestone>(it) }
            .map(Milestone::version)
            .get()
            .also { println("Resolved Gradle milestone: $it") }

    @Serializable
    private data class Milestone(
        val version: String,
    )
}
