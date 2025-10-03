package dev.panuszewski.gradle.fixtures

import dev.panuszewski.gradle.framework.GradleSpec
import dev.panuszewski.gradle.framework.NoConfigFixture

object OverriddenPluginVersion : NoConfigFixture {

    const val pluginId = LibsInPluginsBlock.pluginId
    const val pluginVersion = LibsInPluginsBlock.pluginVersion
    const val pluginMarker = LibsInPluginsBlock.pluginMarker
    const val overriddenPluginVersion = "1.18.15"

    override fun GradleSpec.install() {
        installFixture(LibsInPluginsBlock)

        // and
        val overriddenPluginVersion = "1.18.15"

        // and
        includedBuild {
            buildGradleKts {
                append {
                    """
                    dependencies {
                        implementation("$pluginMarker:$overriddenPluginVersion")
                    }
                    """
                }
            }
        }
    }
}