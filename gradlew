#!/usr/bin/env bash

# Resolve application directory
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# If gradle is available in PATH, use it
if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
fi

# Fallback to wrapper jar if present
WRAPPER_DIR="$DIR/gradle/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/gradle-wrapper.jar"

if [ -f "$WRAPPER_JAR" ]; then
    exec java -Dorg.gradle.appname=gradlew -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
else
    echo "Gradle is not installed in PATH and gradle-wrapper.jar is not found."
    echo "Please install Gradle 8.5+ or run inside the GitHub Actions workflow."
    exit 1
fi
