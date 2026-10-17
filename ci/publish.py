import argparse
import io
import json
import os
import re
import shutil
import stat
import tempfile
import time
import urllib.error
import urllib.request
import zipfile
from pathlib import Path, PurePosixPath

from history import merge
from report import render_dashboard


LIMIT = 100 * 1024 * 1024
ARCHIVE = 'reports.zip'


def download(url):
    for attempt in range(3):
        try:
            request = urllib.request.Request(url, headers={'Cache-Control': 'no-cache'})
            with urllib.request.urlopen(request, timeout=30) as response:
                data = response.read(LIMIT + 1)
            if len(data) > LIMIT:
                raise ValueError('Published site exceeds size limit')
            return data
        except urllib.error.HTTPError as error:
            if error.code == 404:
                return None
            if error.code < 500 or attempt == 2:
                raise
        except (urllib.error.URLError, TimeoutError):
            if attempt == 2:
                raise
        time.sleep(2 ** attempt)


def unpack(data, output):
    with zipfile.ZipFile(io.BytesIO(data)) as archive:
        entries = archive.infolist()
        if sum(entry.file_size for entry in entries) > LIMIT:
            raise ValueError('Expanded site exceeds size limit')
        for entry in entries:
            path = PurePosixPath(entry.filename)
            if (path.is_absolute() or '..' in path.parts or '\\' in entry.filename
                    or stat.S_ISLNK(entry.external_attr >> 16) or entry.filename == ARCHIVE):
                raise ValueError('Unsafe published archive entry')
        archive.extractall(output)


def restore(output, base_url, run):
    suffix = '?run=' + run
    data = download(base_url + '/' + ARCHIVE + suffix)
    if data is not None:
        unpack(data, output)
        return
    for name in ('index.html', 'history.json'):
        data = download(base_url + '/' + name + suffix)
        if data is not None:
            (output / name).write_bytes(data)


def copy_report(source, destination):
    if not (source / 'index.html').is_file():
        raise ValueError('Missing report index.html')
    for path in source.rglob('*'):
        if path.is_symlink():
            raise ValueError('Report must not contain symbolic links')
        if path.is_file() and path.name in ('index.html', 'history.json', 'summary.json', 'manifest.json'):
            target = destination / path.relative_to(source)
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(path, target)


def assemble(source, output, kind, run):
    target = output / kind / run
    if target.exists():
        raise ValueError('This report snapshot already exists')
    if kind == 'tests':
        source = source / 'pages'
    else:
        candidates = [path.parent for path in source.glob('*/index.html')]
        if len(candidates) != 1:
            raise ValueError('Expected exactly one completed load suite')
        source = candidates[0]
    copy_report(source, target)
    if kind == 'tests':
        previous = json.loads((output / 'history.json').read_text()) if (output / 'history.json').exists() else []
        current = json.loads((source / 'history.json').read_text())
        with tempfile.TemporaryDirectory() as directory:
            merged = Path(directory)
            render_dashboard(merged, merge(previous, current))
            for name in ('index.html', 'history.json'):
                shutil.copyfile(merged / name, output / name)
    relative = f'{kind}/{run}/'
    latest = output / kind / 'index.html'
    latest.parent.mkdir(parents=True, exist_ok=True)
    latest.write_text(f'<!doctype html><html lang="ru"><meta charset="utf-8"><meta http-equiv="refresh" content="0; url={run}/"><title>Отчёт</title><a href="{run}/">Открыть отчёт</a></html>')
    if not (output / 'index.html').exists():
        (output / 'index.html').write_text(f'<!doctype html><html lang="ru"><meta charset="utf-8"><title>Отчёты CI</title><a href="{relative}">Открыть отчёт</a></html>')
    paths = [path for path in output.rglob('*') if path.is_file() and path.name != ARCHIVE]
    if sum(path.stat().st_size for path in paths) > LIMIT:
        raise ValueError('Site exceeds size limit; existing deployment remains unchanged')
    with zipfile.ZipFile(output / ARCHIVE, 'w', zipfile.ZIP_DEFLATED) as archive:
        for path in paths:
            archive.write(path, path.relative_to(output))
    return relative


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--input', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--kind', choices=('tests', 'load-testing'), required=True)
    parser.add_argument('--base-url', required=True)
    args = parser.parse_args()
    run = os.environ['GITHUB_RUN_ID'] + '-' + os.environ['GITHUB_RUN_ATTEMPT']
    if not re.fullmatch(r'\d+-\d+', run):
        raise ValueError('Invalid run identifier')
    args.output.mkdir(parents=True, exist_ok=False)
    base_url = args.base_url.rstrip('/')
    restore(args.output, base_url, run)
    relative = assemble(args.input, args.output, args.kind, run)
    with open(os.environ['GITHUB_OUTPUT'], 'a') as stream:
        stream.write(f'report_url={base_url}/{relative}\n')


if __name__ == '__main__':
    main()
