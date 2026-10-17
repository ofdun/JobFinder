import io
import json
import stat
import tempfile
import unittest
import zipfile
from pathlib import Path
from unittest.mock import patch

from publish import ARCHIVE, assemble, restore, unpack


class PublicationStorageTest(unittest.TestCase):
    def test_archive_rejects_traversal_and_links(self):
        for name, mode in [('../escape', 0), ('/escape', 0), ('link', stat.S_IFLNK << 16), ('reports.zip', 0)]:
            data = io.BytesIO()
            with zipfile.ZipFile(data, 'w') as archive:
                entry = zipfile.ZipInfo(name)
                entry.external_attr = mode
                archive.writestr(entry, 'value')
            with tempfile.TemporaryDirectory() as directory:
                with self.assertRaises(ValueError):
                    unpack(data.getvalue(), Path(directory))
                self.assertEqual([], list(Path(directory).iterdir()))

    def test_network_failure_does_not_turn_into_empty_site(self):
        with tempfile.TemporaryDirectory() as directory, patch('publish.download', side_effect=TimeoutError):
            with self.assertRaises(TimeoutError):
                restore(Path(directory), 'https://example.test', '1-1')

    def test_publications_preserve_other_reports_and_test_history(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            site = root / 'site'
            site.mkdir()
            old = {'run': '1.1', 'time': '1', 'stages': []}
            new = {'run': '2.1', 'time': '2', 'stages': []}
            (site / 'history.json').write_text(json.dumps([old]))
            (site / 'index.html').write_text('existing dashboard')
            load = root / 'load/fixture/01-ramp'
            load.mkdir(parents=True)
            (load.parent / 'index.html').write_text('suite')
            (load / 'index.html').write_text('report')
            (load / 'summary.json').write_text('{}')
            (load / 'backend.log').write_text('private log')
            (load / 'resources.jsonl').write_text('{}')
            self.assertEqual('load-testing/10-2/', assemble(root / 'load', site, 'load-testing', '10-2'))
            self.assertEqual('existing dashboard', (site / 'index.html').read_text())
            saved = (site / ARCHIVE).read_bytes()
            restored = root / 'restored'
            restored.mkdir()
            unpack(saved, restored)
            source = root / 'tests/pages'
            source.mkdir(parents=True)
            (source / 'index.html').write_text('new dashboard')
            (source / 'history.json').write_text(json.dumps([new]))
            assemble(root / 'tests', restored, 'tests', '20-1')
            self.assertEqual([old, new], json.loads((restored / 'history.json').read_text()))
            self.assertEqual('report', (restored / 'load-testing/10-2/01-ramp/index.html').read_text())
            self.assertFalse((restored / 'load-testing/10-2/01-ramp/backend.log').exists())
            self.assertFalse((restored / 'load-testing/10-2/01-ramp/resources.jsonl').exists())
            with zipfile.ZipFile(restored / ARCHIVE) as archive:
                self.assertNotIn(ARCHIVE, archive.namelist())
                self.assertIn('tests/20-1/index.html', archive.namelist())
                self.assertIn('load-testing/10-2/01-ramp/summary.json', archive.namelist())

    def test_legacy_site_is_preserved_on_first_publication(self):
        with tempfile.TemporaryDirectory() as directory, patch('publish.download', side_effect=[None, b'index', b'[]']):
            root = Path(directory)
            restore(root, 'https://example.test', '1-1')
            self.assertEqual('index', (root / 'index.html').read_text())
            self.assertEqual('[]', (root / 'history.json').read_text())
