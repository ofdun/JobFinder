import argparse
import csv
import json
import tempfile
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path

from report import generate, histogram, load_run, percentile
from report_view import chart, logarithmic_ticks, shared_histograms
from run import positive


class StatisticsTest(unittest.TestCase):
    def test_percentiles_use_all_requests_and_linear_interpolation(self):
        self.assertEqual(50.5, percentile(list(range(1, 101)), 0.5))
        self.assertEqual(75.25, percentile(list(range(1, 101)), 0.75))
        self.assertAlmostEqual(90.1, percentile(list(range(1, 101)), 0.9))
        self.assertAlmostEqual(95.05, percentile(list(range(1, 101)), 0.95))
        self.assertAlmostEqual(99.01, percentile(list(range(1, 101)), 0.99))
        self.assertEqual(7, percentile([7], 0.99))

    def test_invalid_percentiles_are_rejected(self):
        for data, probability in [([], 0.5), ([1], -0.1), ([1], 1.1)]:
            with self.assertRaises(ValueError):
                percentile(data, probability)

    def test_histogram_preserves_zeros_equal_values_and_outliers(self):
        for data in ([0, 0, 0], [7] * 100, [0, 1, 5, 500, 50_000_001]):
            bins = histogram(data)
            self.assertEqual(len(data), sum(count for _, _, count in bins))
            self.assertTrue(all(low < high for low, high, _ in bins))
        with self.assertRaises(ValueError):
            histogram([-1])

    def test_positive_arguments_reject_zero(self):
        self.assertEqual(100, positive("100"))
        with self.assertRaises(ValueError):
            positive("x")
        with self.assertRaises(argparse.ArgumentTypeError):
            positive("0")

    def test_shared_histogram_uses_same_bins_and_preserves_every_operation(self):
        groups = [[0, 1, 7, 7], [100, 1000, 50_000_001]]
        edges, counts = shared_histograms(groups)
        self.assertEqual(25, len(edges))
        self.assertTrue(all(a < b for a, b in zip(edges, edges[1:])))
        self.assertEqual([len(group) for group in groups], [sum(row) for row in counts])
        self.assertEqual(2, counts[0][0])
        self.assertEqual(1, counts[1][-1])
        for values in ([0] * 10, [7] * 10):
            _, equal_counts = shared_histograms([values, values])
            self.assertEqual(equal_counts[0], equal_counts[1])
            self.assertEqual(10, sum(equal_counts[0]))

    def test_constant_and_zero_measurements_produce_valid_svg(self):
        for value in (0, 0.001, 7):
            for logarithmic in (False, True):
                svg = chart([("Цикл", [(0, value), (0, value)])], "Тест", "мс", "мкс", log_y=logarithmic)
                ET.fromstring(svg)
                self.assertNotIn("nan", svg)
                self.assertNotIn("inf", svg)

    def test_narrow_logarithmic_axes_have_readable_tick_density(self):
        for low, high in [(35000, 85000), (0.1, 0.12), (500, 20000)]:
            ticks = logarithmic_ticks(low, high)
            self.assertGreaterEqual(len(ticks), 3)
            self.assertLessEqual(len(ticks), 7)
            self.assertTrue(all(low <= value <= high for value in ticks))


class ReportTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.directory = Path(self.temporary.name)
        self.config = {"trials": 100, "samples": 2, "warmup_min": 5, "teardown": 3}
        self.rows = []
        cases = [(s, v, n) for s in ("filter", "sum", "best") for v in ("loop", "stream")
                 for n in (512, 4096)] + [("overhead", "harness", 0)]
        for scenario, variant, size in cases:
            for phase, count in [("setup", 0), ("warmup", 5), ("measurement", 200), ("teardown", 3)]:
                self.rows.append(["phase", scenario, variant, size, phase, "", "", "", "", count, 100_000])
            for trial in range(100):
                for request in range(2):
                    self.rows.append(["sample", scenario, variant, size, "measurement", trial, request,
                                      (trial * 2 + request) * 200, 100 + request, "", ""])
        self.save_rows()
        self.manifest = {"status": "complete", "smoke": False, "configuration": self.config,
                         "wall_seconds": 1, "runs": [{"number": 1, "image_id": "fixture-image",
                         "raw_file": "run-001.csv", "wall_seconds": 0.5,
                         "host_config": {"NanoCpus": 1_000_000_000}}]}
        self.save_manifest()

    def save_rows(self):
        with (self.directory / "run-001.csv").open("w", newline="") as out:
            writer = csv.writer(out)
            writer.writerow("kind scenario implementation size phase trial operation elapsed_ns latency_ns operations duration_ns".split())
            writer.writerows(self.rows)

    def save_manifest(self):
        (self.directory / "manifest.json").write_text(json.dumps(self.manifest))

    def test_report_pairs_variants_and_contains_all_required_statistics(self):
        generate(self.directory)
        report = (self.directory / "index.html").read_text()
        summary = json.loads((self.directory / "summary.json").read_text())
        self.assertEqual(23, report.count("<svg"))
        self.assertEqual(7, report.count('data-chart-panel="time"'))
        self.assertEqual(7, report.count('data-chart-panel="percentiles"'))
        self.assertEqual(7, report.count('data-chart-panel="histogram"'))
        self.assertEqual(6, report.count('<details class="case">'))
        self.assertIn("НЕ ДОСТИГНУТ", report)
        self.assertEqual(13, report.count('<td>Teardown</td><td>3</td>'))
        for label in ("p50", "p75", "p90", "p95", "p99", "Warmup", "Setup", "Измерение"):
            self.assertIn(label, report)
        self.assertNotIn("<p>", report)
        self.assertIn('id="run-selector"', report)
        self.assertIn('id="size-selector"', report)
        self.assertEqual(13, len(summary["runs"][0]["cases"]))
        self.assertEqual(200, summary["runs"][0]["cases"]["best/loop/512"]["operations"])

    def test_runs_remain_separate_and_each_size_has_its_own_panel(self):
        self.manifest["runs"].append({**self.manifest["runs"][0], "number": 2, "image_id": "second-image"})
        self.save_manifest()
        generate(self.directory)
        report = (self.directory / "index.html").read_text()
        summary = json.loads((self.directory / "summary.json").read_text())
        self.assertEqual([1, 2], [run["number"] for run in summary["runs"]])
        self.assertEqual(2, report.count('<article data-run='))
        for size in (512, 4096):
            self.assertEqual(2, report.count(f'<section data-size="{size}">'))
        self.assertEqual(46, report.count("<svg"))
        for run in summary["runs"]:
            self.assertEqual(200, run["cases"]["best/loop/512"]["operations"])

    def test_missing_and_duplicate_operations_are_rejected(self):
        self.rows.pop()
        self.save_rows()
        with self.assertRaises(ValueError):
            load_run(self.directory / "run-001.csv", self.config)
        self.rows.append(self.rows[-1])
        self.save_rows()
        with self.assertRaises(ValueError):
            load_run(self.directory / "run-001.csv", self.config)

    def test_wrong_phase_count_is_rejected(self):
        self.rows[2][-2] = 1
        self.save_rows()
        with self.assertRaises(ValueError):
            load_run(self.directory / "run-001.csv", self.config)

    def test_shared_image_and_insufficient_trials_are_rejected(self):
        self.manifest["runs"].append(self.manifest["runs"][0].copy())
        self.save_manifest()
        with self.assertRaises(ValueError):
            generate(self.directory)
        self.manifest["runs"].pop()
        self.manifest["configuration"]["trials"] = 99
        self.save_manifest()
        with self.assertRaises(ValueError):
            generate(self.directory)

    def test_smoke_report_cannot_be_mistaken_for_full_results(self):
        self.manifest["smoke"] = True
        self.save_manifest()
        generate(self.directory)
        self.assertIn("SMOKE CHECK ONLY", (self.directory / "index.html").read_text())


if __name__ == "__main__":
    unittest.main()
