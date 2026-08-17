# Contributing

Thanks for your interest in contributing to Typesafe Conventions Gradle Plugin.

This project is a Gradle plugin for Kotlin DSL convention builds. Good contributions are focused, include test coverage for Gradle behavior, and call out which Gradle versions or convention-plugin scenarios are affected.

## Getting Started

1. Install JDK 17.
2. Fork and clone the repository.
3. Create a branch for your change.

```bash
git clone https://github.com/radoslaw-panuszewski/typesafe-conventions-gradle-plugin.git
cd typesafe-conventions-gradle-plugin
git checkout -b your-change
```

Use the Gradle wrapper from the repository for all local checks:

```bash
./gradlew check
```

## Testing Against Gradle Versions

CI runs `./gradlew check` across a matrix of Gradle versions by setting `GRADLE_VERSION_TO_TEST`. To reproduce a specific version locally, run:

```bash
GRADLE_VERSION_TO_TEST=8.14 ./gradlew check
```

To test without the Gradle build cache, use:

```bash
./gradlew check --no-build-cache
```

## Test Coverage

When changing plugin behavior, prefer adding or updating tests under:

- `src/test/kotlin`
- `src/functionalTest/kotlin`

Functional tests are especially useful for convention-plugin scenarios, version catalog behavior, included builds, and wrong-usage cases.

## Pull Requests

Please keep pull requests focused and include:

- what behavior changed
- why the change is needed
- which Gradle version or versions you tested
- whether the change affects version catalogs, convention catalogs, included builds, or top-level builds

If the change alters generated accessors or compatibility behavior, include a small example or test fixture that demonstrates the expected behavior.
