import argparse
import json
import urllib.error
import urllib.request
from pathlib import Path


def merge(*histories):
    records = {}
    for history in histories:
        if not isinstance(history, list):
            raise ValueError("History must be an array")
        for record in history:
            if not isinstance(record.get("stages"), list):
                raise ValueError("Missing stage results")
            records[record["run"]] = {**records.get(record["run"], {}), **record}
    return sorted(records.values(), key=lambda row: (row["time"], row["run"]))


def restore(path):
    local = json.loads(path.read_text()) if path.exists() else []
    seed = Path(__file__).with_name("history-seed.json")
    previous = json.loads(seed.read_text()) if seed.exists() else []
    try:
        request = urllib.request.Request("https://ofdun.github.io/JobFinder/history.json", headers={"Cache-Control": "no-cache"})
        with urllib.request.urlopen(request, timeout=30) as response:
            previous = merge(previous, json.load(response))
    except urllib.error.HTTPError as error:
        if error.code != 404:
            raise
    history = merge(previous, local)
    if any(not row.get("number") for row in history):
        try:
            request = urllib.request.Request("https://api.github.com/repos/ofdun/JobFinder/actions/runs?per_page=100", headers={"User-Agent": "JobFinder-test-dashboard", "Accept": "application/vnd.github+json"})
            with urllib.request.urlopen(request, timeout=30) as response:
                numbers = {str(run["id"]): run["run_number"] for run in json.load(response)["workflow_runs"]}
            for row in history:
                if not row.get("number"):
                    row["number"] = numbers.get(row["run"].split(".")[0])
        except (urllib.error.URLError, TimeoutError):
            print("Run numbers unavailable; dashboard will show dates")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(history, indent=2) + "\n")
    print(f"Restored {len(history)} historical runs")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--history", type=Path, required=True)
    restore(parser.parse_args().history)
