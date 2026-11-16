#!/usr/bin/env bash
set -euo pipefail
quality_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)
quality_cache="${XDG_CACHE_HOME:-$HOME/.cache}/jobfinder-quality"
quality_fingerprint=$(shasum -a 256 "$quality_root/quality/requirements.txt" | cut -d ' ' -f 1)
quality_env="$quality_cache/$quality_fingerprint"
python3 - "$quality_env" "$quality_root/quality/requirements.txt" <<'PYTHON'
import fcntl
from pathlib import Path
import subprocess
import sys

environment = Path(sys.argv[1])
environment.parent.mkdir(parents=True, exist_ok=True)
with environment.with_suffix(".lock").open("w") as lock:
    fcntl.flock(lock, fcntl.LOCK_EX)
    if not (environment / "ready").exists():
        subprocess.run([sys.executable, "-m", "venv", str(environment)], check=True)
        subprocess.run([
            str(environment / "bin/python"), "-m", "pip", "install",
            "--disable-pip-version-check", "-r", sys.argv[2],
        ], check=True)
        (environment / "ready").touch()
PYTHON
