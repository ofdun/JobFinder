import unittest
import xml.etree.ElementTree as ET

from detail_charts import charts, duration_unit, linear_histograms, prepare


class DetailChartsTest(unittest.TestCase):
    def test_histogram_counts_every_operation_in_shared_linear_bins(self):
        for groups in ([[0, 1, 7], [100, 1000, 50_000_001]], [[0] * 4, [0] * 5],
                       [[7] * 10, [7] * 10]):
            edges, counts = linear_histograms(groups)
            self.assertEqual(25, len(edges))
            self.assertEqual([len(group) for group in groups], [sum(row) for row in counts])
            widths = [b - a for a, b in zip(edges, edges[1:])]
            self.assertTrue(all(width > 0 for width in widths))
            for width in widths:
                self.assertAlmostEqual(widths[0], width)
        for groups in ([], [[]], [[-1]]):
            with self.assertRaises(ValueError):
                linear_histograms(groups)

    def test_trial_means_timestamps_and_full_cdf_are_preserved(self):
        cases = [{"samples": [
            {"trial": 0, "elapsed_ns": 1_000_000, "latency_ns": 1000},
            {"trial": 0, "elapsed_ns": 3_000_000, "latency_ns": 3000},
            {"trial": 1, "elapsed_ns": 5_000_000, "latency_ns": 2000},
            {"trial": 1, "elapsed_ns": 7_000_000, "latency_ns": 4000}]}]
        data = prepare(cases, ["Sort"])
        self.assertEqual("мкс", data["unit"])
        self.assertEqual("мс", data["time_unit"])
        self.assertEqual([(1, 2), (2, 3)], data["trials"][0][1])
        self.assertEqual([(1, 2), (5, 3)], data["times"][0][1])
        points = data["distributions"][0][1]
        self.assertEqual(9, len(points))
        self.assertEqual((0, 0), points[0])
        self.assertEqual((4, 100), points[-1])
        self.assertEqual([25, 50, 75, 100], [point[1] for point in points[2::2]])

    def test_units_change_only_display_scale(self):
        self.assertEqual((1, "нс"), duration_unit([0, 80]))
        self.assertEqual((1000, "мкс"), duration_unit([1000, 9000]))
        self.assertEqual((1_000_000, "мс"), duration_unit([1000, 2_000_000]))

    def test_zero_and_single_sample_generate_finite_charts(self):
        for value in (0, 100, 50_000_001):
            cases = [{"samples": [{"trial": 0, "elapsed_ns": 0, "latency_ns": value}]}]
            root = ET.fromstring(charts(cases, ["Sort"]))
            figures = root.findall("figure")
            self.assertEqual(["trials", "percentiles", "histogram", "time"],
                             [figure.attrib["data-chart-panel"] for figure in figures])
            for element in root.iter():
                for value in element.attrib.values():
                    self.assertNotIn("nan", value)
                    self.assertNotIn("inf", value)


if __name__ == "__main__":
    unittest.main()
