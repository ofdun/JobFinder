import os
import tempfile
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path
from unittest.mock import patch

from report import generate, render_dashboard
from history import merge


class ReportTest(unittest.TestCase):
    def test_dashboard_does_not_count_synthetic_stage_markers_as_tests(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            record = generate(root, root / "report", root / "history.json", {"unit": {"result": "failure"}})
            self.assertEqual([0, 0, 0], [s["actual"]["tests"] for s in record["stages"]])
            self.assertEqual(1, record["stages"][0]["failures"])
            self.assertEqual({"index.html", "history.json"}, {p.name for p in (root / "report/pages").iterdir()})

    def test_history_merges_and_deduplicates_without_a_thirty_run_limit(self):
        rows = [{"run": str(i), "time": f"{i:03}", "stages": []} for i in range(40)]
        self.assertEqual(rows, merge(rows[:30], rows[20:]))

    def test_dashboard_escapes_embedded_script_markup(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            render_dashboard(root, [{"run": "</script><script>alert(1)</script>", "stages": []}])
            self.assertNotIn("</script><script>alert(1)</script>", (root / "index.html").read_text())
            self.assertNotIn("__HISTORY_JSON__", (root / "index.html").read_text())

    def test_failed_stage_preserves_failures_and_marks_downstream_skipped(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            results = root / "inputs/unit-results/test-results/test"
            results.mkdir(parents=True)
            (results / "TEST-example.xml").write_text('<testsuite name="example" tests="1" failures="1" errors="0" skipped="0" time="0.5"><testcase classname="example" name="fails"><failure message="expected"/></testcase></testsuite>')
            record = generate(root / "inputs", root / "report", root / "history.json", {"unit": {"result": "failure"}})
            self.assertEqual([1, 0, 0], [s["failures"] for s in record["stages"]])
            self.assertEqual([0, 1, 1], [s["skipped"] for s in record["stages"]])
            self.assertEqual(2, len(ET.parse(root / "report/junit.xml").findall(".//skipped")))

    def test_missing_results_produce_failure_not_false_success(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            result = generate(root, root / "report", root / "history.json", {"unit": {"result": "success"}})
            self.assertEqual(1, result["stages"][0]["failures"])

    def test_history_retains_separate_runs(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            for run in ("100", "101"):
                with patch.dict(os.environ, {"GITHUB_RUN_ID": run}):
                    generate(root, root / "report", root / "history.json", {})
            import json
            self.assertEqual(2, len(json.loads((root / "history.json").read_text())))


if __name__ == "__main__":
    unittest.main()
