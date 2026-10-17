import os
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path


SCRIPT = Path(__file__).resolve().parents[1] / 'JobFinder/src/test/resources/docker/prepare-ollama.sh'
FAKE_OLLAMA = '''
import os
import signal
import sys
from pathlib import Path

root = Path(os.environ['FAKE_OLLAMA_ROOT'])
command = sys.argv[1]
if command == 'serve':
    def stop(*args):
        (root / 'stopped').touch()
        sys.exit(0)
    signal.signal(signal.SIGTERM, stop)
    (root / 'server').write_text(str(os.getpid()))
    while True:
        signal.pause()
counter = root / command
count = int(counter.read_text()) + 1 if counter.exists() else 1
counter.write_text(str(count))
if command == 'list':
    sys.exit(0 if (root / 'server').exists() and count > int(os.environ['READY_FAILURES']) else 1)
if command == 'pull':
    if sys.argv[2] != 'mxbai-embed-large':
        sys.exit(2)
    if count <= int(os.environ['PULL_FAILURES']):
        print('Error: 503', file=sys.stderr)
        sys.exit(1)
    sys.exit(0)
sys.exit(2)
'''


class OllamaSetupTest(unittest.TestCase):
    def run_setup(self, ready_failures=0, pull_failures=0):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            commands = {
                'ollama': f'#!{sys.executable}\n' + FAKE_OLLAMA,
                'timeout': '#!/bin/sh\nprintf "%s\\n" "$1" >> "$FAKE_OLLAMA_ROOT/timeouts"\nshift\nexec "$@"\n',
                'sleep': '#!/bin/sh\nprintf "%s\\n" "$1" >> "$FAKE_OLLAMA_ROOT/delays"\n',
            }
            for name, content in commands.items():
                path = root / name
                path.write_text(content)
                path.chmod(0o755)
            result = subprocess.run(['sh', str(SCRIPT)], capture_output=True, text=True, timeout=15,
                                    env={**os.environ, 'PATH': f'{root}:{os.environ["PATH"]}',
                                         'FAKE_OLLAMA_ROOT': directory, 'READY_FAILURES': str(ready_failures),
                                         'PULL_FAILURES': str(pull_failures)})
            self.assertTrue((root / 'stopped').exists(), result.stderr)
            with self.assertRaises(ProcessLookupError):
                os.kill(int((root / 'server').read_text()), 0)
            counts = {name: int((root / name).read_text()) if (root / name).exists() else 0
                      for name in ('list', 'pull')}
            delays = (root / 'delays').read_text().splitlines() if (root / 'delays').exists() else []
            timeouts = (root / 'timeouts').read_text().splitlines()
            return result, counts, delays, timeouts

    def test_success_downloads_once_and_stops_server(self):
        result, counts, _, timeouts = self.run_setup()
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual(1, counts['pull'])
        self.assertIn('180', timeouts)

    def test_temporary_503_retries_with_backoff(self):
        result, counts, delays, _ = self.run_setup(pull_failures=2)
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual(3, counts['pull'])
        self.assertEqual(['5', '10'], [delay for delay in delays if delay != '1'])

    def test_persistent_failure_remains_a_failure(self):
        result, counts, delays, _ = self.run_setup(pull_failures=10)
        self.assertEqual(1, result.returncode)
        self.assertEqual(5, counts['pull'])
        self.assertEqual(['5', '10', '20', '40'], [delay for delay in delays if delay != '1'])
        self.assertIn('failed after 5 attempts', result.stderr)

    def test_download_waits_for_readiness(self):
        result, counts, _, _ = self.run_setup(ready_failures=3)
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertGreaterEqual(counts['list'], 4)
        self.assertEqual(1, counts['pull'])

    def test_unready_server_is_bounded_and_never_downloads(self):
        result, counts, _, timeouts = self.run_setup(ready_failures=100)
        self.assertEqual(1, result.returncode)
        self.assertEqual(30, counts['list'])
        self.assertEqual(0, counts['pull'])
        self.assertEqual(['5'] * 30, timeouts)
        self.assertIn('readiness timed out', result.stderr)
