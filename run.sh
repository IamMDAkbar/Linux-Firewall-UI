#!/usr/bin/env bash
# Small helper to build and run the Firewall UI application from the project root.
# Usage:
#   ./run.sh           # builds and runs the app
#   ./run.sh -- help   # passed through as program args

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

if ! command -v mvn >/dev/null 2>&1; then
  echo "Maven (mvn) not found in PATH. Please install Maven or run the app from an IDE."
  exit 1
fi

echo "Building (skip tests) and launching MainFrame..."

# Pass any args to the Java main via -Dexec.args
mvn -q -DskipTests=true exec:java -Dexec.mainClass=com.firewall.MainFrame -Dexec.args="${*}"
