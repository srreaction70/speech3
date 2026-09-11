#!/bin/sh
# Gradle wrapper script for Unix
DIRNAME=`dirname "$0"`
[ -z "$DIRNAME" ] && DIRNAME="."
APP_HOME=`cd "$DIRNAME" && pwd`
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

exec "$JAVACMD" "-Dorg.gradle.appname=gradlew" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
