import os
import tempfile
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path
from unittest.mock import patch

from report import generate


class ReportTest(unittest.TestCase):
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
