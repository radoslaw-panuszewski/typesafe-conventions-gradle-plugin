GRADLE_VERSION=${2:-$(./gradlew --version | grep Gradle | awk '{print $2}')}
TEST_PROJECT_DIR=$(echo "$1" | sed 's/ /-/g')
TEST_PROJECT_PATH=".test-projects/$GRADLE_VERSION/$TEST_PROJECT_DIR"
echo "Opening $TEST_PROJECT_PATH"
idea "$TEST_PROJECT_PATH"
