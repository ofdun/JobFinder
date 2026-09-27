import argparse
import copy
import html
import json
import os
import xml.etree.ElementTree as ET
from datetime import datetime, timezone
from pathlib import Path

TASKS = {"unit": "test", "integration": "integrationTest", "e2e": "e2eTest"}


def stage_report(inputs, stage, status):
    suites = []
    for path in sorted((inputs / f"{stage}-results" / "test-results" / TASKS[stage]).glob("TEST-*.xml")):
        suites.append(ET.parse(path).getroot())
    totals = {key: sum(int(s.get(key, 0)) for s in suites) for key in ("tests", "failures", "errors", "skipped")}
    if status != "success" or not suites:
        suite = ET.Element("testsuite", name=f"pipeline.{stage}", tests="1", failures="0", errors="0", skipped="0")
        case = ET.SubElement(suite, "testcase", classname="pipeline", name=stage)
        if status in ("skipped", "cancelled"):
            ET.SubElement(case, "skipped", message=f"Stage {status}; no test execution claimed")
            suite.set("skipped", "1")
        elif not suites or totals["failures"] + totals["errors"] == 0:
            ET.SubElement(case, "failure", message=f"Stage {status}; infrastructure, cleanup or repeatability failed, or results missing")
            suite.set("failures", "1")
        else:
            suite = None
        if suite is not None:
            suites.append(suite)
    totals = {key: sum(int(s.get(key, 0)) for s in suites) for key in ("tests", "failures", "errors", "skipped")}
    totals["seconds"] = round(sum(float(s.get("time", 0)) for s in suites), 3)
    totals["status"] = status
    totals["stage"] = stage
    return totals, suites


def generate(inputs, output, history_path, statuses):
    output.mkdir(parents=True, exist_ok=True)
    combined = ET.Element("testsuites")
    stages = []
    for stage in TASKS:
        summary, suites = stage_report(inputs, stage, statuses.get(stage, {}).get("result", "skipped"))
        stages.append(summary)
        for suite in suites:
            combined.append(copy.deepcopy(suite))
    ET.indent(combined)
    ET.ElementTree(combined).write(output / "junit.xml", encoding="utf-8", xml_declaration=True)
    record = {
        "run": os.environ.get("GITHUB_RUN_ID", "local") + "." + os.environ.get("GITHUB_RUN_ATTEMPT", "1"),
        "time": datetime.now(timezone.utc).isoformat(),
        "sha": os.environ.get("GITHUB_SHA", "local"),
        "stages": stages,
    }
    history = json.loads(history_path.read_text()) if history_path.exists() else []
    history = [item for item in history if item["run"] != record["run"]][-29:] + [record]
    history_path.parent.mkdir(parents=True, exist_ok=True)
    history_path.write_text(json.dumps(history, indent=2) + "\n")
    (output / "history.json").write_text(json.dumps(history, indent=2) + "\n")
    headings = ("stage", "status", "tests", "failures", "errors", "skipped", "seconds")
    table = "<table><tr>" + "".join(f"<th>{key}</th>" for key in headings) + "</tr>"
    table += "".join("<tr>" + "".join(f"<td>{html.escape(str(row[key]))}</td>" for key in headings) + "</tr>" for row in stages) + "</table>"
    trend = "<table><tr><th>Run</th><th>UTC</th><th>Cases</th><th>Failed</th><th>Skipped</th><th>Seconds</th></tr>"
    for item in history:
        rows = item["stages"]
        values = (item["run"], item["time"], sum(r["tests"] for r in rows), sum(r["failures"] + r["errors"] for r in rows), sum(r["skipped"] for r in rows), round(sum(r["seconds"] for r in rows), 3))
        trend += "<tr>" + "".join(f"<td>{html.escape(str(value))}</td>" for value in values) + "</tr>"
    trend += "</table>"
    (output / "index.html").write_text('<!doctype html><html lang="en"><meta charset="utf-8"><title>Test pipeline</title><style>body{font:16px system-ui;margin:2rem}table{border-collapse:collapse}td,th{border:1px solid #aaa;padding:.5rem;text-align:left}</style><h1>Test pipeline</h1>' + table + '<p>Skipped stages are represented by one synthetic skipped case. Infrastructure failures have a synthetic failed case. Individual assertions are in the stage HTML reports and junit.xml.</p><h2>Run history (last 30)</h2>' + trend + '<p>History is restored from the branch-specific Actions cache when available and included in this artifact.</p></html>')
    summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary_path:
        with open(summary_path, "a") as stream:
            stream.write("## Test results\n\n" + table + "\n\n## Run history\n\n" + trend + "\n")
    return record


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--inputs", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--history", type=Path, required=True)
    args = parser.parse_args()
    generate(args.inputs, args.output, args.history, json.loads(os.environ.get("STAGE_RESULTS", "{}")))
