import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from pipeline import bundle, collect, profiles_for
from plan import PROFILES, cpu_layout, make_plan
from run import IMAGES, pinned_images


class LoadPipelineTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)
        self.source = self.root / 'inputs'
        self.output = self.root / 'report' / 'suite'

    def fixture(self, profile, artifact=None):
        path = self.source / (artifact or profile) / 'timestamp' / ('01-' + profile)
        path.mkdir(parents=True)
        manifest = {'status': 'complete', 'plan': make_plan(profile), 'source_sha256': 'same-source',
                    'base_images': {key: 'shared-' + key for key in IMAGES},
                    'application_settings': {'threads': 32}, 'cpu_sets': cpu_layout(),
                    'containers': {key: {'image': profile + '-' + key, 'resources': {'Memory': 768}}
                                   for key in IMAGES}}
        (path / 'manifest.json').write_text(json.dumps(manifest))
        (path / 'summary.json').write_text(json.dumps({'profile': profile, 'smoke': False}))
        for name in ('index.html', 'samples.gz', 'resources.jsonl', 'backend.log'):
            (path / name).write_text(name)
        return path

    def change(self, path, key, value):
        manifest = json.loads((path / 'manifest.json').read_text())
        manifest[key] = value
        (path / 'manifest.json').write_text(json.dumps(manifest))

    def test_select_all_or_one_profile(self):
        self.assertEqual(list(PROFILES), profiles_for('all'))
        for profile in PROFILES:
            self.assertEqual([profile], profiles_for(profile))
        with self.assertRaises(ValueError):
            profiles_for('../unexpected')

    @patch('pipeline.generate_suite')
    def test_collect_preserves_all_five_results_and_logs(self, render):
        for profile in PROFILES:
            self.fixture(profile)
        collect(self.source, self.output, list(PROFILES))
        self.assertEqual(set(PROFILES), {p.name for p in self.output.iterdir()})
        for profile in PROFILES:
            self.assertEqual('backend.log', (self.output / profile / 'backend.log').read_text())
        render.assert_called_once_with(self.output)

    @patch('pipeline.generate_suite')
    def test_single_profile_can_be_collected(self, render):
        self.fixture('stress')
        collect(self.source, self.output, ['stress'])
        render.assert_called_once_with(self.output)

    def test_missing_and_duplicate_profiles_are_rejected(self):
        self.fixture('ramp')
        with self.assertRaisesRegex(ValueError, 'Missing profiles'):
            collect(self.source, self.output, list(PROFILES))
        self.fixture('ramp', 'duplicate')
        with self.assertRaisesRegex(ValueError, 'duplicate profile'):
            collect(self.source, self.output, ['ramp'])
        self.assertFalse(self.output.exists())

    def test_different_images_sources_or_measurement_settings_are_rejected(self):
        self.fixture('ramp')
        second = self.fixture('stress')
        original = json.loads((second / 'manifest.json').read_text())
        for key, value in [('base_images', {'backend': 'different'}),
                           ('source_sha256', 'other-source'),
                           ('plan', {**make_plan('stress'), 'think_seconds': 2})]:
            with self.subTest(key=key):
                (second / 'manifest.json').write_text(json.dumps(original))
                self.change(second, key, value)
                with self.assertRaises(ValueError):
                    collect(self.source, self.output, ['ramp', 'stress'])
                self.assertFalse(self.output.exists())

    def test_incomplete_run_and_missing_data_are_rejected(self):
        path = self.fixture('ramp')
        self.change(path, 'status', 'failed')
        with self.assertRaisesRegex(ValueError, 'Incomplete run'):
            collect(self.source, self.output, ['ramp'])
        self.change(path, 'status', 'complete')
        (path / 'samples.gz').unlink()
        with self.assertRaisesRegex(ValueError, 'Missing run output'):
            collect(self.source, self.output, ['ramp'])

    def test_symbolic_links_are_rejected_before_copy(self):
        path = self.fixture('ramp')
        (path / 'linked.log').symlink_to(path / 'backend.log')
        with self.assertRaisesRegex(ValueError, 'symbolic links'):
            collect(self.source, self.output, ['ramp'])

    @patch('pipeline.docker')
    @patch('pipeline.prepare_images')
    def test_bundle_exports_exactly_the_prepared_components(self, prepare, docker):
        images = {key: 'shared-' + key for key in IMAGES}
        prepare.return_value = images
        target = self.root / 'bundle'
        bundle(target)
        self.assertEqual(images, json.loads((target / 'images.json').read_text()))
        prepare.assert_called_once_with(target, False)
        docker.assert_called_once_with('image', 'save', '--output', target / 'images.tar', *IMAGES.values())

    @patch('run.inspect')
    def test_loaded_bundle_is_verified_without_rebuilding_or_pulling(self, inspect):
        images = {key: 'shared-' + key for key in IMAGES}
        path = self.root / 'images.json'
        path.write_text(json.dumps(images))
        inspect.side_effect = lambda kind, tag: {'Id': images[next(key for key, value in IMAGES.items() if value == tag)]}
        self.assertEqual(images, pinned_images(path))
        inspect.side_effect = None
        inspect.return_value = {'Id': 'unexpected'}
        with self.assertRaisesRegex(ValueError, 'do not match'):
            pinned_images(path)
        path.write_text('{}')
        with self.assertRaisesRegex(ValueError, 'every component'):
            pinned_images(path)


if __name__ == '__main__':
    unittest.main()
