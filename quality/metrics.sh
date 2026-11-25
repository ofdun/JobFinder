#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/environment.sh"
"$quality_env/bin/python" "$quality_root/quality/metrics.py" --module "$1"
