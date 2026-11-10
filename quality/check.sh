#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/environment.sh"
cd "$quality_root"
"$quality_env/bin/python" quality/metrics.py
for module in JobFinder JobFinder-CLI; do
    bash JobFinder/gradlew -p "$module" spotlessJavaCheck classes testClasses --console=plain
done
