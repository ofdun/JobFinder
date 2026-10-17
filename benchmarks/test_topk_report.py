import csv
import json
import re
import tempfile
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path

from report import generate, load_run
from topk_view import PERCENTILES, limit_value, overview


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

    def test_compact_report_has_all_required_charts_and_controls(self):
        generate(self.directory)
        report = (self.directory / "index.html").read_text()
        self.assertEqual(45, report.count("<svg"))
        self.assertEqual(3, report.count('<section data-limit='))
        self.assertEqual(9, report.count('<section data-size='))
        self.assertNotIn('data-chart-panel="trials"', report)
        self.assertEqual(9, report.count('<summary>Подробная статистика</summary>'))
        self.assertNotIn('data-chart-panel="time"', report)
        for chart in ("percentiles", "histogram"):
            self.assertNotIn(f'data-chart-panel="{chart}"', report)
        diagnostics = (self.directory / "diagnostics.html").read_text()
        self.assertEqual(153, diagnostics.count("<svg"))
        self.assertEqual(27, diagnostics.count('data-chart-panel="trials"'))
        self.assertEqual(27, diagnostics.count('data-chart-panel="time"'))
        for chart in ("percentiles", "histogram"):
            self.assertEqual(27, diagnostics.count(f'data-chart-panel="{chart}"'))
        for content in (report, diagnostics):
            self.assertIn('<h3>Фазы и счётчики</h3>', content)
            self.assertNotIn('Фазы, счётчики и диапазон', content)
            self.assertNotIn('<th>Min, мкс</th>', content)
            self.assertNotIn('<th>Max, мкс</th>', content)
            overhead = content.split('<section class="card overhead">')[1].split('</article>')[0]
            self.assertNotIn('<details', overhead)
            self.assertNotIn('<summary', overhead)
            self.assertNotIn('<svg', overhead)
            self.assertEqual(2, overhead.count('<table>'))
            self.assertNotIn('data-chart="percentiles"', overhead)
            self.assertNotIn('data-chart="histogram"', overhead)
            self.assertNotIn('<button', overhead)
        self.assertEqual(27, report.count('class="scenario-numbers"'))
        blocks = re.findall(r'<details class="case detailed-statistics" open>(.*?)</details>', report, re.S)
        self.assertEqual(9, len(blocks))
        for block in blocks:
            self.assertEqual(3, block.count('<table>'))
            self.assertNotIn('<svg', block)
            self.assertNotIn('<button', block)
            self.assertNotIn('<details', block)
        for selector in ("run", "size", "limit"):
            self.assertIn(f'id="{selector}-selector"', report)
        self.assertNotIn("НЕ ДОСТИГНУТ", report)
        self.assertIn("<h2>Накладные расходы</h2>", report)
        self.assertNotIn("Быстрее по", report)
        self.assertNotIn("Числа и соотношения", report)
        for percentile in PERCENTILES:
            self.assertEqual(9, report.count(f'data-percentile="{percentile}"'))
            self.assertEqual(9, report.count(f'data-chart-panel="{percentile}"'))
            self.assertEqual(9, report.count(f'data-chart="{percentile}"'))
            self.assertIn(f'Сравнение {percentile}', report)
        self.assertNotIn("Рост списка", report)
        self.assertNotIn("Рост списка", diagnostics)
        self.assertNotIn("data-growth-percentile", report)
        self.assertEqual(45, report.count("<button"))
        self.assertEqual(45, diagnostics.count("<button"))
        self.assertNotIn("<p>", report)
        self.assertNotIn("Streams", report)
        self.assertNotIn("SMOKE CHECK ONLY", report)

    def test_comparison_charts_use_the_selected_percentile_values(self):
        stats = {}
        for index, variant in enumerate(("sort", "heap")):
            stats[f"random/{variant}/1000/20"] = {
                "percentiles_ns": {key: (position + 1) * 1000 * (index + 1)
                                   for position, key in enumerate(PERCENTILES.values())}}
        for position, percentile in enumerate(PERCENTILES):
            result = overview(stats, 1000, "20", ["random"], percentile)
            svg = ET.fromstring(result[result.index("<svg"):result.index("</svg>") + 6])
            labels = svg.findall("{http://www.w3.org/2000/svg}text")
            self.assertEqual(str(position + 1), labels[-2].text)
            self.assertEqual(str((position + 1) * 2), labels[-1].text)
            self.assertEqual(f"Сравнение {percentile}", svg.attrib["aria-label"])

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

    def test_smoke_results_are_explicitly_marked(self):
        self.manifest["smoke"] = True
        self.save_manifest()
        generate(self.directory)
        self.assertIn("SMOKE CHECK ONLY", (self.directory / "index.html").read_text())

    def test_zero_timings_do_not_break_comparison_charts(self):
        for row in self.rows:
            if row[0] == "sample":
                row[9] = 0
        self.save_rows()
        generate(self.directory)
        report = (self.directory / "index.html").read_text()
        self.assertEqual(45, report.count("<svg"))
        self.assertNotIn("nan", report)

    def test_runs_are_not_pooled(self):
        self.manifest["runs"].append({**self.manifest["runs"][0], "number": 2, "image_id": "second-image"})
        self.save_manifest()
        generate(self.directory)
        summary = json.loads((self.directory / "summary.json").read_text())
        self.assertEqual([1, 2], [run["number"] for run in summary["runs"]])
        for run in summary["runs"]:
            self.assertEqual(200, run["cases"]["random/sort/1000/20"]["operations"])
        report = (self.directory / "index.html").read_text()
        self.assertEqual(2, report.count('<article data-run='))


if __name__ == "__main__":
    unittest.main()
