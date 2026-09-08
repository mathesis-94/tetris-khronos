#!/bin/bash

JAVA_HOME="${JAVA_HOME:=$(dirname $(dirname $(readlink -f $(which java))))}"
JAR_PATH="target/tetris-khronos-1.0-MILESTONE-1-jar-with-dependencies.jar"

if [ ! -f "$JAR_PATH" ]; then
    echo "Error: JAR not found at $JAR_PATH"
    exit 1
fi

java \
    --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media \
    -jar "$JAR_PATH"
