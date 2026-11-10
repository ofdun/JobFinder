#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/environment.sh"
cd "$quality_root"
existing=$(git config --get core.hooksPath || true)
if [ -n "$existing" ] && [ "$existing" != .githooks ]; then
    printf 'Уже настроен другой каталог Git hooks: %s\n' "$existing" >&2
    exit 1
fi
git config core.hooksPath .githooks
printf '%s\n' 'Проверки перед коммитом подключены.'
