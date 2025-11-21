#!/bin/bash

# Script to test all projects with config-preflight

set -e

VERSION="${1:-}"

echo "=============================================="
echo "Testing ALL projects with config-preflight"
echo "=============================================="

if [ -n "$VERSION" ]; then
    echo "Using version: $VERSION"
else
    echo "Using default version from each pom.xml"
fi

echo ""

# Test Spring Boot
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1/3 - Testing Spring Boot"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
cd spring-boot-test
if [ -n "$VERSION" ]; then
    ./test.sh "$VERSION"
else
    ./test.sh
fi
cd ..

echo ""
echo ""

# Test Quarkus
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2/3 - Testing Quarkus"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
cd quarkus-test
if [ -n "$VERSION" ]; then
    ./test.sh "$VERSION"
else
    ./test.sh
fi
cd ..

echo ""
echo ""

# Test Micronaut
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3/3 - Testing Micronaut"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
cd micronaut-test
if [ -n "$VERSION" ]; then
    ./test.sh "$VERSION"
else
    ./test.sh
fi
cd ..

echo ""
echo ""
echo "=============================================="
echo "✅ All tests completed!"
echo "=============================================="
echo ""
echo "Summary:"
echo "  - Spring Boot: ✓"
echo "  - Quarkus: ✓"
echo "  - Micronaut: ✓"
echo ""
echo "Review the output above for config-preflight validation results."
