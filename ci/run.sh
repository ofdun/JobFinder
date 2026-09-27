#!/usr/bin/env bash
set -euo pipefail

stage=${1:?Usage: bash ci/run.sh unit|integration|e2e}
case "$stage" in unit|integration|e2e) ;; *) exit 2 ;; esac
cd "$(dirname "$0")/.."
root=$(pwd -P)
run_id="jf-test-$(date +%s)-$$-${RANDOM}"
engine="${run_id}-engine"
runner="${run_id}-runner"
output="$root/.ci-artifacts/$stage"
if [ -d "$output" ]; then
    mv "$output" "$root/.ci-artifacts/${stage}-previous-${run_id}"
fi
mkdir -p "$output"

cleanup() {
    result=$?
    trap - EXIT INT TERM
    docker logs "$engine" > "$output/engine.log" 2>&1 || true
    docker rm -fv "$runner" "$engine" > "$output/cleanup.log" 2>&1 || true
    docker network rm "$run_id" >> "$output/cleanup.log" 2>&1 || true
    for container in "$runner" "$engine"; do
        if docker container inspect "$container" >/dev/null 2>&1; then
            printf '%s\n' "Test container was not removed: $container" >> "$output/cleanup.log"
            result=1
        fi
    done
    if docker network inspect "$run_id" >/dev/null 2>&1; then
        printf '%s\n' "Test network was not removed: $run_id" >> "$output/cleanup.log"
        result=1
    fi
    printf '%s\n' "$result" > "$output/exit-code.txt"
    exit "$result"
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

docker build -t jobfinder-test-runner:local ci
docker network create "$run_id" >/dev/null
docker run -d --privileged --name "$engine" --network "$run_id" --network-alias engine \
    -e DOCKER_TLS_CERTDIR= docker:27.5.1-dind --tls=false >/dev/null
ready=false
for attempt in $(seq 1 60); do
    if docker exec "$engine" docker info >/dev/null 2>&1; then ready=true; break; fi
    sleep 1
done
if [ "$ready" != true ]; then exit 1; fi
docker run --name "$runner" --network "$run_id" \
    -e DOCKER_HOST=tcp://engine:2375 -e TESTCONTAINERS_HOST_OVERRIDE=engine \
    -e CI_STAGE="$stage" -e CI_FORCE_FAILURE="${CI_FORCE_FAILURE:-}" \
    -e ARTIFACT_UID="$(id -u)" -e ARTIFACT_GID="$(id -g)" \
    -v "$root:/workspace" jobfinder-test-runner:local "$stage" 2>&1 | tee "$output/run.log"
