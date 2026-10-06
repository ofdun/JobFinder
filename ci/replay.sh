#!/usr/bin/env bash
set -euo pipefail
base=${1:?Missing API URL}
case "$base" in http://localhost:18080/api/v1) ;; *) printf '%s\n' 'Replay is restricted to the isolated test server'; exit 2 ;; esac
body=$(mktemp)
trap 'rm -f "$body"' EXIT
request() {
    expected=$1; method=$2; path=$3; token=${4:-}; payload=${5:-}
    args=(--silent --show-error --max-time 15 --output "$body" --write-out '%{http_code}' -X "$method")
    if [ -n "$token" ]; then args+=(-H "Authorization: Bearer $token"); fi
    if [ -n "$payload" ]; then args+=(-H 'Content-Type: application/json' --data "$payload"); fi
    status=$(curl "${args[@]}" "$base$path")
    printf '%s %s -> %s\n' "$method" "$path" "$status" >&2
    test "$status" = "$expected"
    cat "$body"
}
applicant=$(request 200 POST /auth/applicant/login '' '{"email":"alice@example.com","password":"test-password"}' | jq -er .accessToken)
employer=$(request 200 POST /auth/employer/login '' '{"email":"employer@example.com","password":"test-password"}' | jq -er .accessToken)
request 200 GET /vacancies/1 | jq -e '.id == 1' >/dev/null
id=$(request 201 POST /applications "$applicant" '{"vacancyId":1,"resumeId":1,"applicationStatus":"NEW"}' | jq -er 'select(type == "number" and . > 0)')
request 200 GET "/applications/$id" "$applicant" | jq -e '.applicationStatus == "NEW"' >/dev/null
request 200 PUT "/applications/$id" "$employer" '{"vacancyId":1,"resumeId":1,"applicationStatus":"INVITATION"}' | jq -e '.applicationStatus == "INVITATION"' >/dev/null
request 200 GET "/applications/$id" "$applicant" | jq -e '.applicationStatus == "INVITATION"' >/dev/null
request 403 GET "/applications/$id" >/dev/null
request 204 DELETE "/applications/$id" "$applicant" >/dev/null
printf '%s\n' 'curl replay passed'
