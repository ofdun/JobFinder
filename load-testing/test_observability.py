import unittest

from observability import measure


class ResourceMeasurementTest(unittest.TestCase):
    def test_clips_cpu_and_integrates_memory_at_measurement_boundaries(self):
        rows = [{'component': 'backend', 'time': t, 'cpu_ns': t * 500_000_000,
                 'ram_bytes': (100 + 10 * t) * 2 ** 20} for t in range(5)]
        result = measure(rows, 'backend', 0.5, 3.5)
        self.assertAlmostEqual(result['cpu_seconds'], 1.5)
        self.assertAlmostEqual(result['cpu_percent'], 50)
        self.assertAlmostEqual(result['ram_mean_mib'], 120)
        self.assertAlmostEqual(result['ram_peak_mib'], 135)
        self.assertAlmostEqual(result['sampled_seconds'], 3)

    def test_does_not_count_startup_cpu_or_other_components(self):
        rows = [{'component': name, 'time': t, 'cpu_ns': 90e9 + t * rate,
                 'ram_bytes': 100} for name, rate in [('backend', 1e9), ('monitor', 1e7)] for t in range(4)]
        self.assertAlmostEqual(measure(rows, 'monitor', 1, 3)['cpu_seconds'], 0.02)

    def test_rejects_missing_monitor_samples(self):
        with self.assertRaises(ValueError):
            measure([], 'monitor', 0, 10)

    def test_rejects_large_gaps_and_counter_resets(self):
        for times, counters in [([0, 10], [0, 1e9]), ([0, 1], [1e9, 0])]:
            rows = [{'component': 'backend', 'time': t, 'cpu_ns': c, 'ram_bytes': 1}
                    for t, c in zip(times, counters)]
            with self.assertRaises(ValueError):
                measure(rows, 'backend', times[0], times[-1])


if __name__ == '__main__':
    unittest.main()
