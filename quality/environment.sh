#!/usr/bin/env bash
set -euo pipefail
quality_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)
quality_cache="${XDG_CACHE_HOME:-$HOME/.cache}/jobfinder-quality"
quality_fingerprint=$(shasum -a 256 "$quality_root/quality/requirements.txt" | cut -d ' ' -f 1)
quality_env="$quality_cache/$quality_fingerprint"
if [ ! -f "$quality_env/ready" ]; then
    python3 -m venv "$quality_env"
    "$quality_env/bin/python" -m pip install --disable-pip-version-check -r "$quality_root/quality/requirements.txt"
    touch "$quality_env/ready"
fi
