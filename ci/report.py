import argparse
import copy
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
    actual = dict(totals)
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
    totals["actual"] = actual
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
        "branch": os.environ.get("GITHUB_HEAD_REF") or os.environ.get("GITHUB_REF_NAME", "local"),
        "stages": stages,
    }
    history = json.loads(history_path.read_text()) if history_path.exists() else []
    history = [item for item in history if item["run"] != record["run"]] + [record]
    history.sort(key=lambda item: (item["time"], item["run"]))
    history_path.parent.mkdir(parents=True, exist_ok=True)
    history_path.write_text(json.dumps(history, indent=2) + "\n")
    (output / "history.json").write_text(json.dumps(history, indent=2) + "\n")
    render_dashboard(output, history)
    summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary_path:
        with open(summary_path, "a") as stream:
            stream.write("[Open the test-history dashboard](https://ofdun.github.io/JobFinder/)\n")
    return record


def render_dashboard(output, history):
    output.mkdir(parents=True, exist_ok=True)
    template = Path(__file__).with_name("dashboard.html").read_text()
    data = json.dumps(history).replace("<", "\\u003c").replace("&", "\\u0026")
    (output / "index.html").write_text(template.replace("__HISTORY_JSON__", data))
    (output / "history.json").write_text(json.dumps(history, indent=2) + "\n")
    public = output / "pages"
    public.mkdir(exist_ok=True)
    for name in ("index.html", "history.json"):
        (public / name).write_text((output / name).read_text())


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--inputs", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--history", type=Path, required=True)
    args = parser.parse_args()
    generate(args.inputs, args.output, args.history, json.loads(os.environ.get("STAGE_RESULTS", "{}")))
