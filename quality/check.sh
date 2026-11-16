#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
exec bash JobFinder/gradlew -p . staticAnalysis --console=plain
