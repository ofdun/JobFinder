#!/usr/bin/env bash
set -euo pipefail

stage=${1:?Missing stage}
root=$(pwd -P)
output="$root/.ci-artifacts/$stage"
mkdir -p "$output"
capture_pid=
collect() {
    result=$?
    trap - EXIT
    if [ -n "$capture_pid" ]; then
        kill -INT "$capture_pid" 2>/dev/null || true
        wait "$capture_pid" || true
        if ! tcpdump -nn -r "$root/JobFinder/build/traffic/http.pcap" > "$root/JobFinder/build/traffic/http.txt" 2>&1; then result=1; fi
        if ! grep -q '18080' "$root/JobFinder/build/traffic/http.txt"; then result=1; fi
    fi
    if [ -d "$root/JobFinder/build/reports" ]; then cp -R "$root/JobFinder/build/reports" "$output/"; fi
    if [ -d "$root/JobFinder/build/test-results" ]; then cp -R "$root/JobFinder/build/test-results" "$output/"; fi
    if [ -d "$root/JobFinder/build/traffic" ]; then cp -R "$root/JobFinder/build/traffic" "$output/"; fi
    printf '%s\n' "$result" > "$output/test-exit-code.txt"
    exit "$result"
}
trap collect EXIT

if [ "$stage" = integration ]; then
    docker build -t ollama-mxbai-embed-large:latest -f JobFinder/src/test/resources/docker/ollama.Dockerfile JobFinder
fi
cd JobFinder
case "$stage" in
    unit) sh gradlew --no-daemon --console=plain clean test coverageSummary -PrandomOrder=true -PtestSeed=20260927 ;;
    integration)
        sh gradlew --no-daemon --console=plain clean integrationTest
        cp -R build/test-results/integrationTest "$output/first-run"
        sh gradlew --no-daemon --console=plain integrationTest --rerun-tasks
        python3 ../ci/compare-results.py "$output/first-run" build/test-results/integrationTest
        ;;
    e2e)
        sh gradlew --no-daemon --console=plain clean
        mkdir -p build/traffic
        tcpdump -Z root -i any -U -w build/traffic/http.pcap 'tcp port 18080' > build/traffic/capture.log 2>&1 &
        capture_pid=$!
        sleep 1
        kill -0 "$capture_pid"
        sh gradlew --no-daemon --console=plain e2eTest
        ;;
    *) exit 2 ;;
esac
if [ "${CI_FORCE_FAILURE:-}" = "$stage" ]; then exit 1; fi
