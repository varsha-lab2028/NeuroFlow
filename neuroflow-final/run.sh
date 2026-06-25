#!/usr/bin/env bash
# NeuroFlow — Run the Swing app
# Usage: ./run.sh
# Requires: Java 21+, Maven 3.9+

set -e
cd "$(dirname "$0")"

echo ""
echo "╔══════════════════════════════════════╗"
echo "║         NeuroFlow — Swing App         ║"
echo "╚══════════════════════════════════════╝"
echo ""
echo "Building..."
mvn -q package -DskipTests

echo "Launching..."
echo ""
java -jar target/neuroflow-app-1.0.0.jar
