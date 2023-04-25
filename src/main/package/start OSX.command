#!/bin/bash

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Java is not installed. Please install Java 15 or later."
    read -p "Press [Enter] to exit..."
    exit 1
fi

# Get Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
echo "Java version: $JAVA_VERSION"

# Check if the Java version is at least 15
IFS="." read -ra VERSION_PARTS <<< "$JAVA_VERSION"
MAJOR_VERSION=${VERSION_PARTS[0]}

if [ $MAJOR_VERSION -lt 15 ]; then
    echo "The installed Java version is too old. Please update to at least Java 15."
    read -p "Press [Enter] to exit..."
    exit 1
fi

# Find and run the JAR file
JAR_FILE=$(find . -maxdepth 1 -name "RobotOverlord*-with-dependencies.jar" | head -n 1)

if [ -n "$JAR_FILE" ]; then
    echo "Running JAR file: $JAR_FILE"
    java -jar "$JAR_FILE"
else
    echo "No matching JAR file found."
fi

read -p "Press [Enter] to exit..."
