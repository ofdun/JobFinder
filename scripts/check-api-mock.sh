#!/usr/bin/env bash
set -euo pipefail

# Nested npx must not inherit package/call options from the runtime launcher.
unset npm_config_package npm_config_call NPM_CONFIG_PACKAGE NPM_CONFIG_CALL

PROJECT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
OPENAPI="$PROJECT_DIR/docs/swagger.yml"
ARAZZO="$PROJECT_DIR/docs/api-workflows.arazzo.yml"
PORT="${PORT:-4010}"
MOCK_URL="http://127.0.0.1:$PORT"

# Use a compatible runtime without replacing the user's system Node installation.
if ! node -e 'const [major, minor] = process.versions.node.split(".").map(Number); process.exit((major === 20 && minor >= 19) || (major === 22 && minor >= 12) || major > 22 ? 0 : 1)'; then
  printf 'Using temporary Node 22 for the mock tools...\n'
  exec npx --yes --package=node@22.23.3 -- bash "$PROJECT_DIR/scripts/check-api-mock.sh"
fi
command -v curl >/dev/null
[[ -f "$OPENAPI" && -f "$ARAZZO" ]]

# Resolve the executable first: the background PID will belong to Prism, not npx.
PRISM_BIN="$(npx --yes --package=@stoplight/prism-cli@5.14.2 --call 'command -v prism')"
PRISM_LOG="$(mktemp "${TMPDIR:-/tmp}/jobfinder-prism.XXXXXX")"
PRISM_PID=""
cleanup() {
  if [[ -n "$PRISM_PID" ]]; then
    kill "$PRISM_PID" 2>/dev/null || true
    wait "$PRISM_PID" 2>/dev/null || true
  fi
  rm -f "$PRISM_LOG"
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

printf 'Starting JobFinder OpenAPI mock on %s...\n' "$MOCK_URL"
"$PRISM_BIN" mock -h 127.0.0.1 -p "$PORT" "$OPENAPI" >"$PRISM_LOG" 2>&1 &
PRISM_PID=$!

READY=false
for ((attempt = 0; attempt < 60; attempt++)); do
  if ! kill -0 "$PRISM_PID" 2>/dev/null; then
    cat "$PRISM_LOG" >&2
    exit 1
  fi
  # Confirm our process bound the port before probing it (the port may be occupied).
  if grep -q 'Prism is listening' "$PRISM_LOG" &&
      curl --silent --fail --max-time 2 "$MOCK_URL/api/v2/vacancies" >/dev/null; then
    READY=true
    break
  fi
  sleep 1
done
if [[ "$READY" != true ]]; then
  cat "$PRISM_LOG" >&2
  printf 'Prism did not become ready within the startup timeout.\n' >&2
  exit 1
fi

printf 'Running JobFinder Arazzo workflows...\n'
# Disable optional usage analytics; the contract and requests stay local.
REDOCLY_TELEMETRY=off npx --yes @redocly/cli@2.58.2 respect \
  "$ARAZZO" \
  --server "jobfinder=$MOCK_URL"
