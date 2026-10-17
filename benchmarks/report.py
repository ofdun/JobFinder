import argparse
import csv
import json
import math
import statistics
from collections import defaultdict
from pathlib import Path

from report_view import render


def percentile(values, probability):
    if not values or not 0 <= probability <= 1:
        raise ValueError("nonempty values and a probability in [0, 1] required")
    ordered = sorted(values)
    position = (len(ordered) - 1) * probability
    lower = math.floor(position)
    upper = math.ceil(position)
    return ordered[lower] + (ordered[upper] - ordered[lower]) * (position - lower)


def histogram(values, bins=30):
    if not values or bins < 1 or min(values) < 0:
        raise ValueError("nonnegative measurements and positive bins required")
    low, high = math.log10(max(1, min(values))), math.log10(max(1, max(values)))
    high = max(high, low + 0.01)
    counts = [0] * bins
    for value in values:
        index = min(bins - 1, int((math.log10(max(1, value)) - low) / (high - low) * bins))
        counts[index] += 1
    return [(10 ** (low + i * (high - low) / bins),
             10 ** (low + (i + 1) * (high - low) / bins), count) for i, count in enumerate(counts)]


def load_run(path, config):
    cases = defaultdict(lambda: {"samples": [], "phases": {}})
    with path.open(newline="") as source:
        for row in csv.DictReader(source):
            key = (row["scenario"], row["implementation"], int(row["size"]))
            if config.get("study") == "top-k":
                key += (int(row["limit"]),)
            case = cases[key]
            if row["kind"] == "sample":
                sample = {name: int(row[name]) for name in ("trial", "operation", "elapsed_ns", "latency_ns")}
                if any(value < 0 for value in sample.values()):
                    raise ValueError(f"Negative measurement: {path}")
                case["samples"].append(sample)
            elif row["kind"] == "phase":
                if row["phase"] in case["phases"]:
                    raise ValueError(f"Duplicate phase: {path}")
                case["phases"][row["phase"]] = {name: int(row[name]) for name in ("operations", "duration_ns")}
            else:
                raise ValueError(f"Unknown record: {path}")
    expected = {(scenario, implementation, size) for scenario in ("filter", "sum", "best")
                for implementation in ("loop", "stream") for size in (512, 4096)} | {("overhead", "harness", 0)}
    if config.get("study") == "top-k":
        expected = {(order, implementation, size, size // 2 if limit == "half" else int(limit))
                    for order in config["orders"] for implementation in ("sort", "heap")
                    for size in config["sizes"] for limit in config["limits"]} | {("overhead", "harness", 0, 0)}
    if set(cases) != expected:
        raise ValueError(f"Incomplete scenario matrix: {path}")
    for case in cases.values():
        samples = case["samples"]
        expected_ids = {(trial, sample) for trial in range(config["trials"]) for sample in range(config["samples"])}
        if len(samples) != len(expected_ids) or {(s["trial"], s["operation"]) for s in samples} != expected_ids:
            raise ValueError(f"Missing/duplicate trials or operations: {path}")
        phases = case["phases"]
        if set(phases) != {"setup", "warmup", "measurement", "teardown"}:
            raise ValueError(f"Missing phases: {path}")
        if (phases["setup"]["operations"] != 0 or phases["warmup"]["operations"] < config["warmup_min"]
                or phases["measurement"]["operations"] != len(samples)
                or phases["teardown"]["operations"] != config["teardown"]
                or any(p["duration_ns"] < 0 for p in phases.values())):
            raise ValueError(f"Invalid phase counters: {path}")
        if sum(s["latency_ns"] for s in samples) > phases["measurement"]["duration_ns"]:
            raise ValueError(f"Measurement duration is inconsistent: {path}")
    return cases


def summarize(case):
    values = [sample["latency_ns"] for sample in case["samples"]]
    return {"operations": len(values), "mean_ns": statistics.mean(values),
            "stdev_ns": statistics.stdev(values) if len(values) > 1 else 0,
            "min_ns": min(values), "max_ns": max(values),
            "percentiles_ns": {str(p): percentile(values, p) for p in (0.5, 0.75, 0.9, 0.95, 0.99)},
            "below_100ns": sum(value < 100 for value in values),
            "above_50ms": sum(value > 50_000_000 for value in values),
            "phases": case["phases"]}


def generate(directory):
    directory = Path(directory)
    manifest = json.loads((directory / "manifest.json").read_text())
    if manifest["status"] != "complete" or not manifest["runs"]:
        raise ValueError("A complete run manifest is required")
    images = [run["image_id"] for run in manifest["runs"]]
    if len(set(images)) != len(images):
        raise ValueError("Every run must have a separate image")
    configs = [run["host_config"] for run in manifest["runs"]]
    if any(config != configs[0] for config in configs) or configs[0]["NanoCpus"] != 1_000_000_000:
        raise ValueError("Runs must use the same single-CPU configuration")
    config = manifest["configuration"]
    if not manifest["smoke"] and config["trials"] < 100:
        raise ValueError("At least 100 trials are required")
    summary = {"smoke": manifest["smoke"], "configuration": config, "runs": []}
    datasets = []
    for run in manifest["runs"]:
        cases = load_run(directory / run["raw_file"], config)
        stats = {"/".join(map(str, key)): summarize(case) for key, case in sorted(cases.items())}
        summary["runs"].append({"number": run["number"], "cases": stats})
        datasets.append((run, cases, stats))
    if config.get("study") == "top-k":
        from topk_view import render as render_topk
        content = render_topk(manifest, datasets)
        (directory / "diagnostics.html").write_text(render_topk(manifest, datasets, diagnostics=True))
    else:
        content = render(manifest, datasets)
    (directory / "summary.json").write_text(json.dumps(summary, indent=2) + "\n")
    (directory / "index.html").write_text(content)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    generate(parser.parse_args().directory)
