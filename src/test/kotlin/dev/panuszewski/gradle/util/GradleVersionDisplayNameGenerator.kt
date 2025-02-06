package dev.panuszewski.gradle.util

import dev.panuszewski.gradle.util.GradleVersions.GRADLE_VERSION_TO_TEST
import org.junit.jupiter.api.DisplayNameGenerator
import java.lang.reflect.Method

class GradleVersionDisplayNameGenerator : DisplayNameGenerator.Standard() {

    override fun generateDisplayNameForClass(testClass: Class<*>): String {
        val prefix = "[${GRADLE_VERSION_TO_TEST.version.replaceDotWithWithSimilarSymbol()}]"
        return "$prefix ${super.generateDisplayNameForClass(testClass)}"
    }

    override fun generateDisplayNameForNestedClass(nestedClass: Class<*>): String =
        super.generateDisplayNameForNestedClass(nestedClass)

    override fun generateDisplayNameForMethod(testClass: Class<*>, testMethod: Method): String =
        super.generateDisplayNameForMethod(testClass, testMethod)
}

/**
 * When IntelliJ gets a test name with a dot, it displays only part of the string after last dot (like instead of
 * `[8.12] SomeSpec` it would be `12] SomeSpec`). This little hack replaces dot with a very similar character to
 * trick IntelliJ :P
 */
private fun String.replaceDotWithWithSimilarSymbol() =
    this.replace(".", "․")