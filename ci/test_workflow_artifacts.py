import re
import unittest
from pathlib import Path


class WorkflowArtifactsTest(unittest.TestCase):
    def test_uploads_from_hidden_directories_explicitly_include_hidden_files(self):
        directory = Path(__file__).resolve().parents[1] / '.github' / 'workflows'
        checked = 0
        for workflow in directory.glob('*.yml'):
            for step in re.split(r'(?m)^      - ', workflow.read_text()):
                if not re.search(r'uses: actions/upload-artifact@', step):
                    continue
                path = re.search(r'(?m)^          path: (.+(?:\n            .+)*)', step)
                self.assertIsNotNone(path, workflow.name)
                if re.search(r'(?:^|[\s/])\.[\w-]', path.group(1)):
                    checked += 1
                    with self.subTest(workflow=workflow.name, path=path.group(1)):
                        self.assertRegex(step, r'(?m)^          include-hidden-files: true\s*$')
        self.assertGreater(checked, 0)


if __name__ == '__main__':
    unittest.main()
