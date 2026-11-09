set -eu

ollama serve &
server_pid=$!
cleanup() {
    kill "$server_pid" 2>/dev/null || true
    wait "$server_pid" 2>/dev/null || true
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

attempt=1
until timeout 5 ollama list >/dev/null 2>&1; do
    if ! kill -0 "$server_pid" 2>/dev/null; then
        printf '%s\n' 'Ollama exited before readiness' >&2
        exit 1
    fi
    if [ "$attempt" -ge 30 ]; then
        printf '%s\n' 'Ollama readiness timed out' >&2
        exit 1
    fi
    attempt=$((attempt + 1))
    sleep 1
done

attempt=1
delay=5
until timeout 180 ollama pull mxbai-embed-large; do
    if [ "$attempt" -ge 5 ]; then
        printf '%s\n' 'Model download failed after 5 attempts' >&2
        exit 1
    fi
    printf 'Model download attempt %s failed; retrying in %ss\n' "$attempt" "$delay" >&2
    sleep "$delay"
    attempt=$((attempt + 1))
    delay=$((delay * 2))
done
