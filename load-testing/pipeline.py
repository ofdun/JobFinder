import argparse
import json
import os
import shutil
from pathlib import Path

from plan import PROFILES
from report import generate_suite, validate_run_conditions
from run import IMAGES, docker, prepare_images, write_json


def profiles_for(profile):
    if profile == 'all':
        return list(PROFILES)
    if profile not in PROFILES:
        raise ValueError('Unknown profile')
    return [profile]


def bundle(output):
    output.mkdir(parents=True, exist_ok=False)
    images = prepare_images(output, False)
    write_json(output / 'images.json', images)
    docker('image', 'save', '--output', output / 'images.tar', *IMAGES.values())


def collect(source, output, profiles):
    found = {}
    manifests = []
    for path in sorted(source.rglob('manifest.json')):
        manifest = json.loads(path.read_text())
        profile = manifest['plan']['profile']
        if profile not in profiles or profile in found:
            raise ValueError('Unexpected or duplicate profile: ' + profile)
        for name in ('index.html', 'summary.json', 'samples.gz', 'resources.jsonl'):
            if not (path.parent / name).is_file():
                raise ValueError('Missing run output: ' + name)
        summary = json.loads((path.parent / 'summary.json').read_text())
        if summary['profile'] != profile or summary['smoke'] != manifest['plan']['smoke']:
            raise ValueError('Summary does not match its run')
        found[profile] = path.parent
        manifests.append(manifest)
    if set(found) != set(profiles):
        raise ValueError('Missing profiles: ' + ', '.join(sorted(set(profiles) - set(found))))
    validate_run_conditions(manifests)
    if len({m['source_sha256'] for m in manifests}) != 1:
        raise ValueError('Runs must use identical sources')
    settings = [{key: value for key, value in m['plan'].items()
                 if key not in ('profile', 'phases', 'duration_seconds', 'error_percent')} for m in manifests]
    if any(settings[0] != item for item in settings[1:]):
        raise ValueError('Runs must use identical measurement settings')
    if any(path.is_symlink() for root in found.values() for path in [root, *root.rglob('*')]):
        raise ValueError('Run artifacts must not contain symbolic links')
    output.mkdir(parents=True, exist_ok=False)
    for profile in profiles:
        shutil.copytree(found[profile], output / profile)
    generate_suite(output)


def main():
    parser = argparse.ArgumentParser()
    commands = parser.add_subparsers(dest='command', required=True)
    select = commands.add_parser('profiles')
    select.add_argument('--profile', choices=('all', *PROFILES), default='all')
    prepare = commands.add_parser('bundle')
    prepare.add_argument('--output', type=Path, required=True)
    combine = commands.add_parser('collect')
    combine.add_argument('--input', type=Path, required=True)
    combine.add_argument('--output', type=Path, required=True)
    combine.add_argument('--profile', choices=('all', *PROFILES), default='all')
    args = parser.parse_args()
    if args.command == 'profiles':
        value = json.dumps(profiles_for(args.profile))
        with open(os.environ['GITHUB_OUTPUT'], 'a') as stream:
            stream.write('profiles=' + value + '\n')
    elif args.command == 'bundle':
        bundle(args.output)
    else:
        collect(args.input, args.output, profiles_for(args.profile))


if __name__ == '__main__':
    main()
