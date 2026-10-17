import argparse
import hashlib
import json
import shutil
import subprocess
import sys
import time
import uuid
from datetime import datetime, timezone
from pathlib import Path

from report import generate


def positive(value):
    number = int(value)
    if number < 1:
        raise argparse.ArgumentTypeError("must be positive")
    return number


def execute(arguments, **kwargs):
    return subprocess.run(arguments, check=True, text=True, **kwargs)


def inspect(kind, name):
    return json.loads(execute(["docker", kind, "inspect", name], capture_output=True).stdout)[0]


def main():
    parser = argparse.ArgumentParser(description="Isolated single-CPU vacancy microbenchmarks")
    parser.add_argument("--runs", type=positive, default=2)
    parser.add_argument("--trials", type=positive, default=100)
    parser.add_argument("--samples", type=positive, default=4)
    parser.add_argument("--warmup", type=positive, default=64)
    parser.add_argument("--teardown", type=positive, default=16)
    parser.add_argument("--seed", type=int, default=20260927)
    parser.add_argument("--cpu", type=int, default=0)
    parser.add_argument("--output", type=Path)
    parser.add_argument("--smoke", action="store_true")
    args = parser.parse_args()
    if not args.smoke and args.trials < 100:
        parser.error("a full run requires at least 100 trials per case")
    if args.cpu < 0:
        parser.error("CPU index must be nonnegative")
    if shutil.which("docker") is None:
        parser.error("Docker CLI is required")
    if args.smoke:
        args.trials, args.samples, args.warmup, args.teardown = 2, 4, 32, 8
    root = Path(__file__).resolve().parent
    token = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ") + "-" + uuid.uuid4().hex[:8]
    output = args.output or root / "results" / token
    output.mkdir(parents=True, exist_ok=False)
    output = output.resolve()
    base = "jobfinder-microbench:" + token
    owned_images = [base]
    owned_containers = []
    source_hash = hashlib.sha256()
    sources = sorted(root.glob("src/**/*.java")) + sorted(root.glob("*.py")) + sorted(root.glob("report.*"))
    sources = sorted(set(sources))
    for path in sources + [root / "Dockerfile", root / "Run.Dockerfile", root / ".dockerignore"]:
        source_hash.update(path.relative_to(root).as_posix().encode())
        source_hash.update(path.read_bytes())
    manifest = {
        "schema": 2, "study": "top-k", "status": "running", "smoke": args.smoke,
        "started_at": datetime.now(timezone.utc).isoformat(),
        "source_sha256": source_hash.hexdigest(),
        "configuration": {"study": "top-k", "sizes": [1000, 10000, 100000],
                          "limits": ["20", "100", "half"], "orders": ["random", "ascending", "descending"],
                          "trials": args.trials, "samples": args.samples, "warmup_min": args.warmup,
                          "teardown": args.teardown, "seed": args.seed, "cpu": args.cpu},
        "runs": [],
    }
    started = time.monotonic()
    try:
        manifest["docker"] = json.loads(execute(
            ["docker", "info", "--format", "{{json .}}"], capture_output=True).stdout)
        manifest["docker"] = {key: manifest["docker"].get(key) for key in
                              ("ServerVersion", "OperatingSystem", "Architecture", "NCPU", "MemTotal")}
        print(f"Building benchmark image; output: {output}", flush=True)
        with (output / "build.log").open("w") as log:
            execute(["docker", "build", "-t", base, "-f", str(root / "Dockerfile"), str(root)],
                    stdout=log, stderr=subprocess.STDOUT)
        base_info = inspect("image", base)
        manifest["base_image_id"] = base_info["Id"]
        manifest["jvm_command"] = base_info["Config"]["Entrypoint"]
        for number in range(args.runs):
            image = base + f"-{number + 1}"
            container = "jf-microbench-" + token + f"-{number + 1}"
            owned_images.append(image)
            owned_containers.append(container)
            print(f"Run {number + 1}/{args.runs}: fresh image and container, CPU {args.cpu}", flush=True)
            with (output / "build.log").open("a") as log:
                execute(["docker", "build", "-t", image, "-f", str(root / "Run.Dockerfile"),
                         "--build-arg", "BASE_IMAGE=" + base, "--build-arg", "RUN_ID=" + container,
                         str(root)], stdout=log, stderr=subprocess.STDOUT)
            arguments = [str(x) for x in (args.trials, args.samples, args.warmup,
                                          args.teardown, args.seed, number)] + [str(args.smoke).lower()]
            execute(["docker", "create", "--name", container, "--network=none", "--cpus=1",
                     f"--cpuset-cpus={args.cpu}", "--memory=512m", "--memory-swap=512m",
                     "--pids-limit=128", "--read-only", "--tmpfs", "/tmp:rw,nosuid,size=64m",
                     "--cap-drop=ALL", "--security-opt=no-new-privileges", "--user=65534:65534",
                     image, *arguments], capture_output=True)
            info = inspect("container", container)
            raw_file = f"run-{number + 1:03d}.csv"
            entry = {"number": number + 1, "image_id": info["Image"], "container_id": info["Id"],
                     "raw_file": raw_file, "arguments": arguments,
                     "host_config": {key: info["HostConfig"][key] for key in
                                     ("NanoCpus", "CpusetCpus", "Memory", "MemorySwap", "NetworkMode",
                                      "ReadonlyRootfs", "PidsLimit")}}
            manifest["runs"].append(entry)
            tick = time.monotonic()
            with (output / raw_file).open("w") as raw, (output / f"run-{number + 1:03d}.log").open("w") as log:
                execute(["docker", "start", "-a", container], stdout=raw, stderr=log)
            entry["wall_seconds"] = time.monotonic() - tick
            entry["exit_code"] = inspect("container", container)["State"]["ExitCode"]
            if entry["exit_code"] != 0:
                raise RuntimeError(f"Benchmark exited with {entry['exit_code']}")
            entry["runtime"] = (output / f"run-{number + 1:03d}.log").read_text()
            execute(["docker", "rm", container], capture_output=True)
            owned_containers.remove(container)
        manifest["status"] = "complete"
    except (Exception, KeyboardInterrupt) as error:
        manifest["status"] = "failed"
        manifest["error"] = str(error)
        raise
    finally:
        manifest["wall_seconds"] = time.monotonic() - started
        with (output / "cleanup.log").open("w") as log:
            for container in owned_containers:
                subprocess.run(["docker", "rm", "-f", container], stdout=log, stderr=log)
            for image in reversed(owned_images):
                subprocess.run(["docker", "image", "rm", image], stdout=log, stderr=log)
        (output / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n")
    generate(output)
    print(f"Report: {output / 'index.html'}", flush=True)


if __name__ == "__main__":
    try:
        main()
    except (Exception, KeyboardInterrupt) as error:
        print(f"Benchmark failed: {error}", file=sys.stderr)
        sys.exit(1)
