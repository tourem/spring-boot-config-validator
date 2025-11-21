#!/bin/bash

# Script to test Quarkus application with config-preflight

set -e

echo "=========================================="
echo "Testing Quarkus with config-preflight"
echo "=========================================="

# Determine the version to use
if [ -n "$1" ]; then
    VERSION="$1"
    echo "Using specified version: $VERSION"
else
    # Try to detect if a release version exists, otherwise use SNAPSHOT
    VERSION=$(mvn -q -Dexec.executable=echo -Dexec.args='${config-preflight.version}' --non-recursive exec:exec 2>/dev/null || echo "1.0.0-SNAPSHOT")
    echo "Using version from pom.xml: $VERSION"
fi

# Update the version in pom.xml if provided
if [ -n "$1" ]; then
    echo "Updating config-preflight version to: $VERSION"
    mvn versions:set-property -Dproperty=config-preflight.version -DnewVersion="$VERSION" -DgenerateBackupPoms=false
fi

echo ""
echo "Building and running tests..."
echo ""

# Clean and test
mvn clean test

echo ""
echo "=========================================="
echo "Test completed!"
echo "=========================================="
echo ""
echo "Check the output above for config-preflight validation results."
echo "Missing properties should be detected and reported."
