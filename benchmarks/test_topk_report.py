import csv
import json
import tempfile
import unittest
from pathlib import Path

from report import generate, load_run
from topk_view import limit_value


class TopKReportTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.directory = Path(self.temporary.name)
        self.config = {"study": "top-k", "sizes": [1000, 10000, 100000],
                       "limits": ["20", "100", "half"], "orders": ["random", "ascending", "descending"],
                       "trials": 100, "samples": 2, "warmup_min": 64, "teardown": 16}
        self.rows = []
        cases = [(order, variant, size, limit_value(mode, size))
                 for order in self.config["orders"] for variant in ("sort", "heap")
                 for size in self.config["sizes"] for mode in self.config["limits"]]
        cases.append(("overhead", "harness", 0, 0))
        for order, variant, size, limit in cases:
            for phase, count in [("setup", 0), ("warmup", 64), ("measurement", 200), ("teardown", 16)]:
                self.rows.append(["phase", order, variant, size, limit, phase, "", "", "", "", count, 1_000_000])
            for trial in range(100):
                for request in range(2):
                    latency = (1000 if variant == "sort" else 2000) + request
                    self.rows.append(["sample", order, variant, size, limit, "measurement", trial,
                                      request, (trial * 2 + request) * 3000, latency, "", ""])
        self.save_rows()
        self.manifest = {"schema": 2, "study": "top-k", "status": "complete", "smoke": False,
                         "configuration": self.config, "wall_seconds": 2,
                         "runs": [{"number": 1, "image_id": "topk-fixture", "raw_file": "run-001.csv",
                                   "wall_seconds": 1, "host_config": {"NanoCpus": 1_000_000_000}}]}
        self.save_manifest()

    def save_rows(self):
        with (self.directory / "run-001.csv").open("w", newline="") as out:
            writer = csv.writer(out)
            writer.writerow("kind scenario implementation size limit phase trial operation elapsed_ns latency_ns operations duration_ns".split())
            writer.writerows(self.rows)

    def save_manifest(self):
        (self.directory / "manifest.json").write_text(json.dumps(self.manifest))

    def test_all_54_cases_and_baseline_have_individual_statistics(self):
        generate(self.directory)
        summary = json.loads((self.directory / "summary.json").read_text())
        cases = summary["runs"][0]["cases"]
        self.assertEqual(55, len(cases))
        for info in cases.values():
            self.assertEqual(200, info["operations"])
            self.assertEqual({"0.5", "0.75", "0.9", "0.95", "0.99"}, set(info["percentiles_ns"]))
            self.assertEqual(64, info["phases"]["warmup"]["operations"])
            self.assertEqual(16, info["phases"]["teardown"]["operations"])
        self.assertEqual(1000.5, cases["random/sort/1000/20"]["percentiles_ns"]["0.5"])
        self.assertEqual(2000.5, cases["random/heap/1000/20"]["percentiles_ns"]["0.5"])
        for size in self.config["sizes"]:
            self.assertIn(f"ascending/heap/{size}/{size // 2}", cases)


    def test_wrong_limit_and_missing_order_are_rejected(self):
        self.rows[-1][4] = 1
        self.save_rows()
        with self.assertRaises(ValueError):
            load_run(self.directory / "run-001.csv", self.config)
        self.rows[-1][4] = 0
        self.rows = [row for row in self.rows if row[1] != "ascending"]
        self.save_rows()
        with self.assertRaises(ValueError):
            load_run(self.directory / "run-001.csv", self.config)


    def test_runs_are_not_pooled(self):
        self.manifest["runs"].append({**self.manifest["runs"][0], "number": 2, "image_id": "second-image"})
        self.save_manifest()
        generate(self.directory)
        summary = json.loads((self.directory / "summary.json").read_text())
        self.assertEqual([1, 2], [run["number"] for run in summary["runs"]])
        for run in summary["runs"]:
            self.assertEqual(200, run["cases"]["random/sort/1000/20"]["operations"])


if __name__ == "__main__":
    unittest.main()
