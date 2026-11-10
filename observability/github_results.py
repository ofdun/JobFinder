import argparse
import json
import os
from pathlib import Path
import shutil
import stat
import subprocess
import tempfile
import time
import urllib.error
import urllib.parse
import urllib.request
import zipfile

from import_results import import_results

ROOT = Path(__file__).resolve().parent
API = 'https://api.github.com'


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, request, file, code, message, headers, new_url):
        return None


def github_token():
    token = os.environ.get('GH_TOKEN') or os.environ.get('GITHUB_TOKEN')
    if token:
        return token
    credentials = subprocess.run(
        ['git', 'credential', 'fill'], input='protocol=https\nhost=github.com\n\n',
        capture_output=True, text=True, check=True, timeout=30,
        env={**os.environ, 'GIT_TERMINAL_PROMPT': '0'})
    values = dict(line.split('=', 1) for line in credentials.stdout.splitlines() if '=' in line)
    if not values.get('password'):
        raise ValueError('Настройте авторизацию GitHub или переменную GH_TOKEN')
    return values['password']


def api(path, token):
    request = urllib.request.Request(API + path, headers={
        'Authorization': 'Bearer ' + token, 'Accept': 'application/vnd.github+json'})
    with urllib.request.urlopen(request, timeout=60) as response:
        return json.load(response)


def wait_for_run(repository, run_id, token, wait):
    previous = None
    while True:
        run = api(f'/repos/{repository}/actions/runs/{run_id}', token)
        if run['path'].split('@')[0] != '.github/workflows/load-testing.yml':
            raise ValueError('Указан запуск другого workflow')
        status = run['status']
        if status != previous:
            print(f'{run["html_url"]}: {status}', flush=True)
            previous = status
        if status == 'completed':
            return run
        if not wait:
            raise ValueError('Измерения ещё выполняются; используйте --wait')
        time.sleep(30)


def download(repository, artifact_id, token, destination):
    request = urllib.request.Request(
        f'{API}/repos/{repository}/actions/artifacts/{artifact_id}/zip',
        headers={'Authorization': 'Bearer ' + token})
    try:
        response = urllib.request.build_opener(NoRedirect).open(request, timeout=60)
    except urllib.error.HTTPError as error:
        if error.code != 302:
            raise
        location = error.headers['Location']
        if urllib.parse.urlparse(location).scheme != 'https':
            raise ValueError('Небезопасный адрес скачивания artifact')
        response = urllib.request.urlopen(location, timeout=120)
    with response, destination.open('wb') as output:
        shutil.copyfileobj(response, output)


def extract(archive, destination):
    with zipfile.ZipFile(archive) as bundle:
        if sum(item.file_size for item in bundle.infolist()) > 8 * 1024 ** 3:
            raise ValueError('Результаты превышают 8 ГБ')
        for item in bundle.infolist():
            target = (destination / item.filename).resolve()
            if not target.is_relative_to(destination.resolve()) or stat.S_ISLNK(item.external_attr >> 16):
                raise ValueError('Недопустимый путь в artifact')
        bundle.extractall(destination)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('run_id', type=int)
    parser.add_argument('--repository', default='ofdun/JobFinder')
    parser.add_argument('--project', default='project')
    parser.add_argument('--wait', action='store_true')
    args = parser.parse_args()
    token = github_token()
    run = wait_for_run(args.repository, args.run_id, token, args.wait)
    artifacts = api(f'/repos/{args.repository}/actions/runs/{args.run_id}/artifacts?per_page=100', token)
    selected = [a for a in artifacts['artifacts']
                if a['name'] == f'load-results-{args.run_id}-suite' and not a['expired']]
    if len(selected) != 1:
        raise ValueError(f'Нет результатов измерений: {run["conclusion"]}')
    destination = ROOT / 'results' / f'github-{args.run_id}-{run["run_attempt"]}'
    destination.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(dir=destination.parent) as temporary:
        directory = Path(temporary)
        archive = directory / 'artifact.zip'
        download(args.repository, selected[0]['id'], token, archive)
        extracted = directory / 'data'
        extracted.mkdir()
        extract(archive, extracted)
        candidates = list(extracted.rglob('comparison.json'))
        if len(candidates) != 1:
            raise ValueError('Artifact не содержит одну завершённую серию сравнений')
        source = candidates[0].parent
        sys_path = str(ROOT.parent / 'load-testing')
        import sys
        sys.path.insert(0, sys_path)
        from observability import compare
        compare(source)
        subprocess.run(['docker', 'compose', '-p', args.project, '-f', str(ROOT / 'compose.yml'),
                        'up', '-d', '--wait', 'grafana', 'metrics-db'], check=True)
        import_results(source, destination.name, args.project)
        if destination.exists():
            shutil.rmtree(destination)
        shutil.move(str(source), destination)
    print(f'Grafana: http://localhost:3000/d/comparison/comparison?var-suite={destination.name}', flush=True)
    if run['conclusion'] != 'success':
        print(f'Измерения загружены, но итог workflow: {run["conclusion"]}', flush=True)


if __name__ == '__main__':
    main()
