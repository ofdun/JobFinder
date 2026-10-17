import argparse
import hashlib
import json
import os
import secrets
import subprocess
import time
from datetime import datetime, timezone
from pathlib import Path

from plan import PROFILES, cpu_layout, make_plan


ROOT = Path(__file__).resolve().parent
REPOSITORY = ROOT.parent
IMAGES = {'backend': 'jobfinder-load-base:local', 'postgres': 'postgres:15-alpine',
          'redis': 'redis:7-alpine', 'mongodb': 'mongo:7.0', 'qdrant': 'qdrant/qdrant:v1.17.0',
          'generator': 'grafana/k6:2.0.0', 'monitor': 'jobfinder-load-monitor:local'}


def docker(*args, **kwargs):
    return subprocess.run(['docker', *map(str, args)], check=True, text=True, **kwargs)


def inspect(kind, name):
    return json.loads(docker(kind, 'inspect', name, capture_output=True).stdout)[0]


def write_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n')


def prepare_images(output, skip_build):
    with (output / 'build.log').open('w') as log:
        if not skip_build:
            docker('build', '-t', IMAGES['backend'], '-f', ROOT / 'Dockerfile', REPOSITORY, stdout=log, stderr=log)
            docker('build', '-t', IMAGES['monitor'], '-f', ROOT / 'Monitor.Dockerfile', ROOT, stdout=log, stderr=log)
        for name, image in IMAGES.items():
            try:
                inspect('image', image)
            except subprocess.CalledProcessError:
                if name in ('backend', 'monitor'):
                    raise
                docker('pull', image, stdout=log, stderr=log)
    return {name: inspect('image', image)['Id'] for name, image in IMAGES.items()}


def wait_for_backend(container, seconds=180):
    deadline = time.monotonic() + seconds
    while time.monotonic() < deadline:
        if not inspect('container', container)['State']['Running']:
            raise RuntimeError('Backend exited before readiness')
        try:
            response = docker('exec', container, 'curl', '-fsS', '--max-time', '3',
                              'http://localhost:8080/api/v1/vacancies/1', capture_output=True)
            if json.loads(response.stdout).get('id') == 1:
                return
        except (subprocess.CalledProcessError, ValueError):
            pass
        time.sleep(2)
    raise TimeoutError('Backend readiness exceeded 180 seconds')


def run_one(directory, plan, images, cpus, index):
    directory.mkdir()
    write_json(directory / 'plan.json', plan)
    token = 'jf-load-' + secrets.token_hex(6)
    network = token + '-network'
    derived = []
    run_images = {}
    containers = {}
    manifest = {'status': 'running', 'plan': plan, 'started_at': datetime.now(timezone.utc).isoformat(),
                'cpu_sets': cpus, 'containers': {}, 'base_images': images, 'gpu': 'not used',
                'source_sha256': hashlib.sha256(b''.join(p.read_bytes() for p in sorted(ROOT.glob('*')) if p.is_file())).hexdigest()}
    process = None
    network_created = False
    started = time.monotonic()
    write_json(directory / 'manifest.json', manifest)

    def create(name, image, memory, cpu_group='application', extra=(), command=()):
        container = token + '-' + name
        args = ['create', '--name', container, '--label', 'com.ofdun.jobfinder.load=' + token,
                '--network', network, '--network-alias', name, '--cpuset-cpus', cpus[cpu_group],
                '--cpus', '1' if cpu_group != 'generator' else str(len(cpus[cpu_group].split(','))),
                '--memory', memory, '--memory-swap', memory, '--pids-limit', '512',
                *extra, image, *command]
        docker(*args, capture_output=True)
        containers[name] = container
        info = inspect('container', container)
        manifest['containers'][name] = {'id': info['Id'], 'image': info['Image'],
                                      'resources': {k: info['HostConfig'][k] for k in
                                                    ('NanoCpus', 'CpusetCpus', 'Memory', 'MemorySwap', 'PidsLimit')}}
        return container

    try:
        with (directory / 'image-build.log').open('w') as log:
            for component in ('backend', 'generator'):
                base_tag = token + '-base-' + component
                docker('tag', images[component], base_tag)
                derived.append(base_tag)
                tag = token + '-' + component
                derived.append(tag)
                run_images[component] = tag
                docker('build', '-t', tag, '-f', ROOT / 'Run.Dockerfile', '--build-arg', 'BASE_IMAGE=' + base_tag,
                       '--build-arg', 'RUN_ID=' + token, ROOT, stdout=log, stderr=log)
        docker('network', 'create', '--internal', '--label', 'com.ofdun.jobfinder.load=' + token, network, capture_output=True)
        network_created = True
        create('postgres', images['postgres'], '256m', extra=('-e', 'POSTGRES_DB=test', '-e', 'POSTGRES_USER=test', '-e', 'POSTGRES_PASSWORD=test'))
        create('redis', images['redis'], '128m', command=('redis-server', '--save', '', '--appendonly', 'yes', '--appendfsync', 'everysec'))
        create('mongodb', images['mongodb'], '256m', command=('mongod', '--wiredTigerCacheSizeGB', '0.25', '--bind_ip_all'))
        create('qdrant', images['qdrant'], '256m')
        for name in ('postgres', 'redis', 'mongodb', 'qdrant'):
            docker('start', containers[name], capture_output=True)
        deadline = time.monotonic() + 60
        while True:
            result = subprocess.run(['docker', 'exec', containers['postgres'], 'pg_isready', '-U', 'test', '-d', 'test'], capture_output=True)
            if result.returncode == 0:
                break
            if time.monotonic() > deadline:
                raise TimeoutError('PostgreSQL readiness failed')
            time.sleep(1)
        environment = {'SPRING_PROFILES_ACTIVE': 'postgres', 'SPRING_FLYWAY_LOCATIONS': 'filesystem:/fixtures',
                       'DB_HOST': 'postgres', 'DB_NAME': 'test', 'DB_USER': 'test', 'DB_PASSWORD': 'test',
                       'SPRING_DATA_REDIS_HOST': 'redis', 'SPRING_DATA_REDIS_PORT': '6379',
                       'SPRING_MONGODB_URI': 'mongodb://mongodb:27017/test',
                       'SPRING_DATA_MONGODB_URI': 'mongodb://mongodb:27017/test',
                       'APP_QDRANT_BASE': 'qdrant', 'APP_QDRANT_EMBEDDING_DIMENSION': '1024',
                       'APP_QDRANT_COLLECTION_NAME': 'load-test', 'JWT_SECRET': secrets.token_urlsafe(64),
                       'SERVER_TOMCAT_THREADS_MAX': '32', 'SERVER_TOMCAT_ACCEPT_COUNT': '32',
                       'SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE': '8', 'LOGGING_LEVEL_ROOT': 'WARN',
                       'LOGGING_FILE_NAME': '/tmp/application.log'}
        extra = []
        for key, value in environment.items():
            extra += ['-e', key + '=' + value]
        manifest['application_settings'] = {key: value for key, value in environment.items() if key not in ('JWT_SECRET', 'DB_PASSWORD')}
        create('backend', run_images['backend'], '768m', extra=extra)
        docker('start', containers['backend'], capture_output=True)
        wait_for_backend(containers['backend'])
        with (ROOT / 'seed.sql').open() as source, (directory / 'seed.log').open('w') as log:
            docker('exec', '-i', containers['postgres'], 'psql', '-v', 'ON_ERROR_STOP=1', '-U', 'test', '-d', 'test', stdin=source, stdout=log, stderr=log)
        points = {'points': [{'id': 1000 + i, 'vector': [1.0] + [0.0] * 1023} for i in range(1, 1025)]}
        response = docker('exec', '-i', containers['backend'], 'curl', '-fsS', '--max-time', '60',
                          '-X', 'PUT', '-H', 'Content-Type: application/json', '--data-binary', '@-',
                          'http://qdrant:6333/collections/load-test/points?wait=true',
                          input=json.dumps(points), capture_output=True)
        if json.loads(response.stdout).get('status') != 'ok':
            raise RuntimeError('Vector fixture initialization failed')
        create('generator', run_images['generator'], '512m', 'generator', extra=(
            '--user', '0:0', '-v', f'{ROOT}:/scripts:ro', '-v', f'{directory}:/results',
            '-e', 'BASE_URL=http://backend:8080', '-e', 'K6_NO_USAGE_REPORT=true'), command=(
            'run', '--no-usage-report', '--out', 'json=/results/samples.gz', '/scripts/journey.k6.js'))
        create('monitor', images['monitor'], '128m', 'monitor', extra=(
            '-v', '/var/run/docker.sock:/var/run/docker.sock:ro', '-v', f'{directory}:/results'))
        write_json(directory / 'targets.json', [{'name': name, 'id': data['id']} for name, data in manifest['containers'].items()])
        write_json(directory / 'manifest.json', manifest)
        docker('start', containers['monitor'], capture_output=True)
        with (directory / 'k6.log').open('w') as log:
            process = subprocess.Popen(['docker', 'start', '-a', containers['generator']], stdout=log, stderr=log)
            deadline = time.monotonic() + plan['duration_seconds'] + 90
            while process.poll() is None:
                if time.monotonic() > deadline:
                    raise TimeoutError('Load generator exceeded planned duration')
                try:
                    process.wait(timeout=20)
                except subprocess.TimeoutExpired:
                    print(f'{index}: {plan["profile"]}, running; {max(0, deadline - time.monotonic() - 90):.0f}s planned remaining', flush=True)
        info = inspect('container', containers['generator'])
        manifest['exit_code'] = info['State']['ExitCode']
        manifest['status'] = 'complete' if manifest['exit_code'] in (0, 99) else 'failed'
        manifest['thresholds_passed'] = manifest['exit_code'] == 0
        if manifest['status'] != 'complete':
            raise RuntimeError(f'k6 exited with {manifest["exit_code"]}')
    except BaseException as error:
        manifest['status'] = 'failed'
        manifest['error'] = str(error)
        raise
    finally:
        (directory / 'stop-monitor').touch()
        if 'monitor' in containers:
            subprocess.run(['docker', 'stop', '-t', '8', containers['monitor']], capture_output=True)
        if process is not None and process.poll() is None:
            process.terminate()
        manifest['wall_seconds'] = time.monotonic() - started
        manifest['finished_at'] = datetime.now(timezone.utc).isoformat()
        for name, container in containers.items():
            info = inspect('container', container)
            manifest['containers'][name]['oom_killed'] = info['State'].get('OOMKilled', False)
            with (directory / f'{name}.log').open('w') as log:
                subprocess.run(['docker', 'logs', container], stdout=log, stderr=log)
        write_json(directory / 'manifest.json', manifest)
        for container in reversed(list(containers.values())):
            docker('rm', '-f', '-v', container, capture_output=True)
        if network_created:
            docker('network', 'rm', network, capture_output=True)
        for tag in reversed(derived):
            subprocess.run(['docker', 'image', 'rm', tag], capture_output=True)
    from report import generate
    generate(directory)
    print(f'{index}: {plan["profile"]} complete: {directory / "index.html"}', flush=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--profile', choices=('all', *PROFILES), default='all')
    parser.add_argument('--smoke', action='store_true')
    parser.add_argument('--stable-vus', type=int, default=4)
    parser.add_argument('--peak-vus', type=int, default=128)
    parser.add_argument('--soak-seconds', type=int, default=1800)
    parser.add_argument('--recovery-seconds', type=int, default=600)
    parser.add_argument('--repeat', type=int, default=1)
    parser.add_argument('--skip-build', action='store_true')
    parser.add_argument('--application-cpus', default='0,1')
    parser.add_argument('--generator-cpus', default='2,3')
    parser.add_argument('--monitor-cpus', default='4')
    args = parser.parse_args()
    cpus = cpu_layout(args.application_cpus, args.generator_cpus, args.monitor_cpus)
    if args.repeat < 1:
        parser.error('repeat must be positive')
    profiles = PROFILES if args.profile == 'all' else (args.profile,)
    plans = [make_plan(name, args.smoke, args.stable_vus,
                       min(args.peak_vus, 8) if args.smoke else args.peak_vus,
                       args.soak_seconds, args.recovery_seconds) for name in profiles]
    info = json.loads(docker('info', '--format', '{{json .}}', capture_output=True).stdout)
    if max(int(cpu) for group in cpus.values() for cpu in group.split(',')) >= info['NCPU']:
        parser.error('Not enough Docker VM CPUs for disjoint CPU sets')
    output = ROOT / 'results' / (datetime.now(timezone.utc).strftime('%Y%m%dT%H%M%SZ') + '-' + secrets.token_hex(3))
    output.mkdir(parents=True)
    background = docker('ps', '--format', '{{.Names}}', capture_output=True).stdout.splitlines()
    write_json(output / 'environment.json', {'docker': {k: info.get(k) for k in ('ServerVersion', 'OperatingSystem', 'Architecture', 'NCPU', 'MemTotal')},
                                           'background_containers': background, 'cpu_sets': cpus,
                                           'gpu': 'not used', 'host': os.uname().sysname,
                                           'isolation_scope': 'disjoint Docker VM CPU sets for this test; background containers unchanged'})
    print('Results:', output, flush=True)
    images = prepare_images(output, args.skip_build)
    for attempt in range(args.repeat):
        for plan in plans:
            name = f'{attempt + 1:02d}-{plan["profile"]}'
            print('Starting:', name, flush=True)
            run_one(output / name, plan, images, cpus, name)
    from report import generate_suite
    generate_suite(output)
    print('Report:', output / 'index.html', flush=True)


if __name__ == '__main__':
    main()
