import copy
import unittest

from monitor import counters
from plan import PROFILES, cpu_layout, make_plan
from report import describe, phase_ranges, quantile, recovery, resource_summary, summarize, validate_run_conditions, windows


class LoadStatisticsTest(unittest.TestCase):
    def test_required_profiles_include_long_runs_with_both_error_fractions(self):
        self.assertEqual(('ramp', 'stress', 'soak', 'soak-10', 'soak-20'), PROFILES)
        for profile, fraction in [('soak', 0), ('soak-10', 10), ('soak-20', 20)]:
            plan = make_plan(profile)
            self.assertEqual(fraction, plan['error_percent'])
            self.assertEqual(1800, plan['phases'][-1]['seconds'])
            self.assertFalse(plan['smoke'])
        with self.assertRaises(ValueError):
            make_plan('soak', soak_seconds=10)

    def test_smoke_is_marked_and_never_uses_full_duration(self):
        for profile in PROFILES:
            plan = make_plan(profile, smoke=True, peak_vus=8)
            self.assertTrue(plan['smoke'])
            self.assertLess(plan['duration_seconds'], 60)

    def test_recovery_observation_can_be_extended_without_changing_load(self):
        plan = make_plan('stress', recovery_seconds=900)
        self.assertEqual(900, plan['phases'][-1]['seconds'])
        self.assertEqual(4, plan['phases'][-1]['vus'])
        self.assertEqual(128, next(p['vus'] for p in plan['phases'] if p['name'] == 'overload'))
        with self.assertRaises(ValueError):
            make_plan('stress', recovery_seconds=20)

    def test_cpu_groups_are_disjoint_and_validated(self):
        self.assertEqual({'application': '0,1', 'generator': '2,3', 'monitor': '4'}, cpu_layout())
        for groups in [('0,1', '1,2', '3'), ('0', '1', '0'), ('-1', '2', '3'), ('x', '2', '3')]:
            with self.assertRaises(ValueError):
                cpu_layout(*groups)

    def test_percentiles_interpolate_individual_measurements(self):
        self.assertEqual(50.5, quantile(list(range(1, 101)), 0.5))
        self.assertEqual(95.05, quantile(list(range(1, 101)), 0.95))
        self.assertEqual(2, describe([1, 3])['median'])
        self.assertIsNone(describe([None]))
        for values, probability in [([], 0.5), ([1], 1.1)]:
            with self.assertRaises(ValueError):
                quantile(values, probability)

    def test_rps_includes_empty_windows_and_normalizes_partial_window(self):
        samples = [{'time': t, 'latency_ms': 10, 'unexpected': 'false', 'expected_error': 'false'} for t in [1, 2, 24]]
        data = windows(samples, 0, 25, 10)
        self.assertEqual([0.2, 0, 0.2], [w['rps'] for w in data])
        self.assertIsNone(data[1]['p95_ms'])
        self.assertEqual(5, data[-1]['seconds'])

    def test_intentional_errors_are_not_reported_as_system_failures(self):
        samples = [dict(time=1, latency_ms=20, expected_error='false', unexpected='false'),
                   dict(time=2, latency_ms=10, expected_error='true', unexpected='false'),
                   dict(time=3, latency_ms=3000, expected_error='false', unexpected='true')]
        window = windows(samples, 0, 10, 10)[0]
        self.assertEqual(1 / 3, window['unexpected_rate'])
        self.assertEqual(1 / 3, window['expected_error_rate'])
        self.assertEqual(0.1, window['successful_rps'])
        self.assertEqual(2851, window['p95_ms'])

    def test_summary_and_rps_use_the_same_completion_time_boundaries(self):
        plan = make_plan('soak', smoke=True, peak_vus=8)
        samples = [dict(time=t, latency_ms=latency, expected_error='false', unexpected='false',
                        operation='login', phase=phase, stage=stage)
                   for t, latency, phase, stage in [(4.9, 100, 'warmup', 'warmup'),
                                                    (5.1, 20, 'warmup', 'warmup'),
                                                    (19.9, 30, 'measurement', 'steady'),
                                                    (20.1, 1000, 'measurement', 'steady')]]
        result = summarize(samples, 0, [], {'plan': plan})
        self.assertEqual(2, result['requests'])
        self.assertEqual(result['requests'], sum(w['requests'] for w in result['windows']))
        self.assertEqual(25, result['latency_ms']['mean'])
        self.assertEqual([1, 2], [p['requests'] for p in result['phases']])

    def test_recovery_requires_real_overload_and_three_good_windows(self):
        plan = make_plan('stress', smoke=True, peak_vus=8)
        phases = phase_ranges(plan, 0)
        data = []
        for p in phases:
            for t in range(int(p['start']), int(p['end']) - 1, 2):
                data.append(dict(time=t, seconds=2, requests=40, p95_ms=1000 if p['name'] == 'overload' else 10,
                                 unexpected_rate=0, successful_rps=20))
        result = recovery(data, phases, plan)
        self.assertEqual('recovered', result['state'])
        self.assertEqual(0, result['seconds'])
        self.assertEqual(6, result['confirmed_after_seconds'])
        for window in data:
            window['p95_ms'] = 10
        self.assertEqual('overload_not_observed', recovery(data, phases, plan)['state'])

    def test_recovery_requires_baseline_throughput(self):
        plan = make_plan('stress', smoke=True, peak_vus=8)
        phases = phase_ranges(plan, 0)
        data = []
        for p in phases:
            for t in range(int(p['start']), int(p['end']) - 1, 2):
                data.append(dict(time=t, seconds=2, requests=40, p95_ms=1000 if p['name'] == 'overload' else 10,
                                 unexpected_rate=0, successful_rps=1 if p['name'] == 'recovery' else 20))
        self.assertEqual('not_recovered_within_observation', recovery(data, phases, plan)['state'])

    def test_resources_keep_missing_disk_data_distinct_from_zero(self):
        raw = counters({'memory_stats': {'usage': 1000, 'stats': {'inactive_file': 100}}, 'blkio_stats': {}})
        self.assertEqual(900, raw['ram_bytes'])
        self.assertIsNone(raw['disk_read_bytes'])
        self.assertEqual(0, counters({'blkio_stats': {'io_service_bytes_recursive': None}})['disk_read_bytes'])
        rows = [dict(time=t, component='backend', ram_bytes=100 + t * 10, cpu_percent=t,
                     disk_read_bytes=None, disk_write_bytes=100 + 2 * t, disk_write_bytes_per_second=2)
                for t in [0, 1, 2]]
        data = resource_summary(rows, 0, 2)['backend']
        self.assertIsNone(data['disk_read_bytes_per_second'])
        self.assertEqual(110, data['ram_bytes']['median'])
        self.assertEqual(10, data['ram_bytes_per_second_slope'])
        self.assertEqual(4, data['disk_write_bytes_total'])

    def test_runs_require_distinct_images_and_identical_limits(self):
        first = {'status': 'complete', 'base_images': {'backend': 'base', 'generator': 'k6'},
                 'application_settings': {'threads': 32}, 'cpu_sets': cpu_layout(),
                 'containers': {'backend': {'image': 'app-1', 'resources': {'Memory': 768}},
                                'generator': {'image': 'k6-1', 'resources': {'Memory': 512}}}}
        second = copy.deepcopy(first)
        second['containers']['backend']['image'] = 'app-2'
        second['containers']['generator']['image'] = 'k6-2'
        validate_run_conditions([first, second])
        for component in ('backend', 'generator'):
            duplicate = copy.deepcopy(second)
            duplicate['containers'][component]['image'] = first['containers'][component]['image']
            with self.assertRaises(ValueError):
                validate_run_conditions([first, duplicate])
        second['containers']['backend']['resources']['Memory'] = 1024
        with self.assertRaises(ValueError):
            validate_run_conditions([first, second])

    def test_incomplete_runs_cannot_be_presented_as_comparable_results(self):
        with self.assertRaises(ValueError):
            validate_run_conditions([{'status': 'failed'}])


if __name__ == '__main__':
    unittest.main()
