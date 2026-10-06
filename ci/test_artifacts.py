import os
import subprocess
import tempfile
import unittest
from pathlib import Path


class ArtifactOwnershipTest(unittest.TestCase):
    def check_collection(self, exit_code):
        uid = int(os.environ.get("ARTIFACT_TEST_UID", os.getuid()))
        gid = int(os.environ.get("ARTIFACT_TEST_GID", os.getgid()))
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            project = root / "JobFinder"
            project.mkdir()
            (project / "gradlew").write_text(
                "umask 077\n"
                "mkdir -p build/test-results/test/binary build/reports/tests/test\n"
                "printf results > build/test-results/test/binary/results-generic.bin\n"
                "printf report > build/reports/tests/test/index.html\n"
                f"exit {exit_code}\n"
            )
            result = subprocess.run(
                ["bash", str(Path(__file__).with_name("inside.sh")), "unit"],
                cwd=root,
                env={**os.environ, "ARTIFACT_UID": str(uid), "ARTIFACT_GID": str(gid), "CI_FORCE_FAILURE": "none"},
                capture_output=True,
                text=True,
            )
            self.assertEqual(exit_code, result.returncode, result.stderr)
            output = root / ".ci-artifacts" / "unit"
            for relative in ("test-results/test/binary/results-generic.bin", "reports/tests/test/index.html"):
                artifact = output / relative
                self.assertEqual((uid, gid), (artifact.stat().st_uid, artifact.stat().st_gid))
                self.assertEqual(0o600, artifact.stat().st_mode & 0o777)
                if os.geteuid() == 0:
                    read = subprocess.run(
                        ["cat", str(artifact.relative_to(output))],
                        cwd=output,
                        user=uid,
                        group=gid,
                        extra_groups=[],
                        capture_output=True,
                        text=True,
                    )
                    self.assertEqual(0, read.returncode, read.stderr)
            self.assertEqual(str(exit_code), (output / "test-exit-code.txt").read_text().strip())

    def test_success_artifacts_are_owned_by_runner(self):
        self.check_collection(0)

    def test_failure_artifacts_are_owned_by_runner(self):
        self.check_collection(7)
