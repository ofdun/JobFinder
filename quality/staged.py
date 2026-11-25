import os
from pathlib import Path
import subprocess
import tempfile


def main():
    with tempfile.TemporaryDirectory(prefix="jobfinder-staged-") as directory:
        subprocess.run(
            ["git", "checkout-index", "--all", "--prefix", directory + os.sep], check=True
        )
        root = Path(directory)
        environment = dict(os.environ)
        variables = subprocess.check_output(
            ["git", "rev-parse", "--local-env-vars"], text=True
        ).splitlines()
        for name in variables:
            environment.pop(name, None)
        result = subprocess.run(["bash", "JobFinder/gradlew", "-p", ".", "staticAnalysis", "--console=plain"], cwd=root, env=environment)
        if result.returncode:
            print("Коммит отменён: исправьте ошибки статического анализа.")
        return result.returncode


if __name__ == "__main__":
    raise SystemExit(main())
