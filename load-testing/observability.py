import argparse
import html
import json
import secrets
import shutil
from datetime import datetime, timezone
from pathlib import Path

from plan import PROFILES, cpu_layout, make_plan
from report import table, TITLES
from urllib.parse import urlencode
from run import ROOT, IMAGES, docker, machine_info, prepare_images, run_one, write_json


MODES = {
    'default': {'tracing': False, 'extended_logging': False},
    'tracing': {'tracing': True, 'extended_logging': False},
    'logging': {'tracing': False, 'extended_logging': True},
    'combined': {'tracing': True, 'extended_logging': True},
}
LABELS = {'default': 'Обычные логи', 'tracing': 'Обычные логи + трейсы',
          'logging': 'Расширенные логи', 'combined': 'Расширенные логи + трейсы'}


def page(title, body):
    css = (ROOT.parent / 'benchmarks/report.css').read_text()
    return ('<!doctype html><html lang="ru"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">'
            f'<title>{html.escape(title)}</title><style>{css}</style><main><h1>{html.escape(title)}</h1>'
            + body + '</main></html>')


def measure(records, component, start, stop):
    rows = sorted((r for r in records if r['component'] == component and 'error' not in r),
                  key=lambda r: r['time'])
    cpu = ram = seconds = 0.0
    peak = 0
    for left, right in zip(rows, rows[1:]):
        lo, hi = max(start, left['time']), min(stop, right['time'])
        interval = right['time'] - left['time']
        if hi <= lo or interval <= 0 or interval > 3 or right['cpu_ns'] < left['cpu_ns']:
            continue
        fraction = (hi - lo) / interval
        cpu += (right['cpu_ns'] - left['cpu_ns']) * fraction / 1e9
        lo_ram = left['ram_bytes'] + (right['ram_bytes'] - left['ram_bytes']) * (lo - left['time']) / interval
        hi_ram = left['ram_bytes'] + (right['ram_bytes'] - left['ram_bytes']) * (hi - left['time']) / interval
        ram += (lo_ram + hi_ram) / 2 * (hi - lo)
        peak = max(peak, lo_ram, hi_ram)
        seconds += hi - lo
    if seconds < (stop - start) * 0.9:
        raise ValueError(f'Insufficient resource samples for {component}: {seconds:.1f}s')
    return {'cpu_seconds': cpu, 'cpu_percent': cpu / seconds * 100,
            'ram_mean_mib': ram / seconds / 2 ** 20, 'ram_peak_mib': peak / 2 ** 20,
            'sampled_seconds': seconds}


def trace_summary(directory, start, stop):
    path = directory / 'traces.jsonl'
    count = 0
    previews = {}
    if path.exists():
        with path.open() as source:
            for line in source:
                for resource in json.loads(line).get('resourceSpans', []):
                    for scope in resource.get('scopeSpans', []):
                        for span in scope.get('spans', []):
                            began = int(span['startTimeUnixNano']) / 1e9
                            if not start <= began <= stop:
                                continue
                            count += 1
                            trace = span['traceId']
                            if trace not in previews and len(previews) >= 30:
                                continue
                            previews.setdefault(trace, []).append({
                                'name': span['name'], 'span': span['spanId'],
                                'parent': span.get('parentSpanId', ''), 'start': began,
                                'duration_ms': (int(span['endTimeUnixNano']) - int(span['startTimeUnixNano'])) / 1e6,
                                'status': span.get('status', {}).get('code', 0)})
    body = '<a href="../../index.html">Сравнение</a>'
    body += f'<p>Спанов за измерение: {count}. Полный экспорт OTLP — в артефакте CI, traces.jsonl.</p>'
    for trace, spans in previews.items():
        origin = min(s['start'] for s in spans)
        rows = [[html.escape(s['name']), html.escape(s['span']), html.escape(s['parent']),
                 f'{(s["start"] - origin) * 1000:.3f}', f'{s["duration_ms"]:.3f}', str(s['status'])]
                for s in sorted(spans, key=lambda s: s['start'])]
        body += '<h2>' + html.escape(trace) + '</h2>' + table(
            ['Операция', 'Span ID', 'Родитель', 'Начало, мс', 'Длительность, мс', 'Статус'], rows)
    (directory / 'traces').mkdir(exist_ok=True)
    (directory / 'traces/index.html').write_text(page('Трейсы', body))
    return count


def log_summary(directory, start, stop):
    result = {'records': 0, 'debug_records': 0, 'correlated_records': 0, 'bytes': 0}
    with (directory / 'backend.log').open() as source:
        for line in source:
            try:
                record = json.loads(line)
                when = datetime.fromisoformat(record['timestamp'].replace('Z', '+00:00')).timestamp()
            except (ValueError, KeyError, TypeError):
                continue
            if start <= when <= stop:
                result['records'] += 1
                result['bytes'] += len(line.encode())
                result['debug_records'] += record.get('level') == 'DEBUG'
                result['correlated_records'] += bool(record.get('traceId'))
    return result


def verify_telemetry(root, config, summary):
    metrics = json.loads((root / 'k6-summary.json').read_text())['metrics']
    if metrics.get('journeys_completed', {}).get('values', {}).get('count', 0) == 0:
        raise ValueError('No complete user journeys: ' + root.name)
    start, stop = summary['measurement_start'], summary['measurement_stop']
    spans = trace_summary(root, start, stop)
    logs = log_summary(root, start, stop)
    if config['tracing'] and not spans:
        raise ValueError('No measured traces exported: ' + root.name)
    if not config['tracing'] and (spans or logs['correlated_records']):
        raise ValueError('Tracing is still active in disabled configuration')
    if config['extended_logging'] and not logs['debug_records']:
        raise ValueError('Extended logging produced no DEBUG records')
    if config['tracing'] and not logs['correlated_records']:
        raise ValueError('Trace IDs are missing in application logs')
    return spans, logs


def compare(directory):
    results = []
    reference = None
    groups = {}
    for path in sorted(directory.glob('*/manifest.json')):
        root = path.parent
        manifest = json.loads(path.read_text())
        config = manifest['observation']
        mode = next(k for k, v in MODES.items() if v == config)
        profile = manifest['plan']['profile']
        key = (root.name.split('-')[0], profile)
        group = groups.setdefault(key, {})
        if mode in group or manifest['status'] != 'complete':
            raise ValueError('Duplicate or incomplete comparison run')
        settings = {k: v for k, v in manifest['application_settings'].items()
                    if not k.startswith(('TRACING_', 'LOGGING_LEVEL_', 'SPRING_AUTOCONFIGURE_EXCLUDE'))}
        conditions = [manifest['base_images'], manifest['cpu_sets'], manifest['machine'],
                      manifest['source_sha256'], settings,
                      {k: v['resources'] for k, v in manifest['containers'].items() if k != 'collector'}]
        if reference is not None and reference != conditions:
            raise ValueError('Comparison requires identical sources, images, resources and machine')
        reference = conditions
        summary = json.loads((root / 'summary.json').read_text())
        records = [json.loads(line) for line in (root / 'resources.jsonl').read_text().splitlines()]
        start, stop = summary['measurement_start'], summary['measurement_stop']
        components = ('backend', 'monitor', 'collector') if config['tracing'] else ('backend', 'monitor')
        resources = {name: measure(records, name, start, stop) for name in components}
        spans, logs = verify_telemetry(root, config, summary)
        item = {'run': root.name, 'profile': profile, 'mode': mode, 'plan': manifest['plan'],
                'smoke': summary['smoke'], 'resources': resources, 'spans': spans, 'logs': logs,
                'requests': summary['requests'], 'unexpected_errors': summary['unexpected_errors'],
                'rps': summary['rps']['mean'] if summary['rps'] else 0,
                'p95_ms': summary['latency_ms']['percentiles']['0.95'] if summary['latency_ms'] else None}
        group[mode] = item
        results.append(item)
    if not groups:
        raise ValueError('No comparison results')
    differences = []
    for (attempt, profile), group in groups.items():
        if set(group) != set(MODES) or any(r['plan'] != group['default']['plan'] for r in group.values()):
            raise ValueError('Each profile requires all four modes with identical load plans')
        for left, right, label in [('default', 'tracing', 'Трейсы / обычные логи'),
                                   ('logging', 'combined', 'Трейсы / расширенные логи'),
                                   ('default', 'logging', 'Логирование / без трейсов'),
                                   ('tracing', 'combined', 'Логирование / с трейсами')]:
            a, b = group[left]['resources']['backend'], group[right]['resources']['backend']
            differences.append({'attempt': attempt, 'profile': profile, 'effect': label,
                                **{k: b[k] - a[k] for k in ('cpu_seconds', 'ram_mean_mib', 'ram_peak_mib')}})
    write_json(directory / 'comparison.json', {'runs': results, 'differences': differences})
    rows = []
    for r in results:
        for component, values in r['resources'].items():
            rows.append([f'<a href="{r["run"]}/index.html">{TITLES[r["profile"]]}</a>',
                         LABELS[r['mode']], component,
                         *[f'{values[k] * (2 ** 20 / 1e6 if k.startswith("ram_") else 1000 if k == "cpu_seconds" else 1):.2f}' for k in ('cpu_seconds', 'cpu_percent', 'ram_mean_mib', 'ram_peak_mib')],
                         f'{values["sampled_seconds"]:.1f}'])
    body = '<p>' + ('SMOKE' if results[0]['smoke'] else 'Полный прогон') + '</p>'
    body += '<p><a href="http://localhost:3000/d/comparison/comparison">Grafana</a></p>'
    body += '<h2>Ресурсы</h2>' + table(['Сценарий', 'Настройки', 'Компонент', 'CPU, мс', 'CPU, % ядра',
                                     'RAM средняя, МБ', 'RAM пик, МБ', 'Измерено, с'], rows)
    body += '<h2>Разница относительно парного прогона</h2>' + table(
        ['Повтор', 'Сценарий', 'Изменение', 'Δ CPU, мс', 'Δ RAM средняя, МБ', 'Δ RAM пик, МБ'],
        [[d['attempt'], TITLES[d['profile']], d['effect'], *[f'{d[k] * (2 ** 20 / 1e6 if k.startswith("ram_") else 1000 if k == "cpu_seconds" else 1):+.2f}' for k in
          ('cpu_seconds', 'ram_mean_mib', 'ram_peak_mib')]] for d in differences])
    body += '<h2>Нагрузка и трейсы</h2>' + table(
        ['Сценарий', 'Настройки', 'Запросы', 'RPS', 'p95, мс', 'Неожиданные ошибки', 'Спаны'],
        [[TITLES[r['profile']], LABELS[r['mode']], str(r['requests']), f'{r["rps"]:.2f}',
          '—' if r['p95_ms'] is None else f'{r["p95_ms"]:.2f}', str(r['unexpected_errors']),
          f'<a href="{r["run"]}/traces/index.html">{r["spans"]}</a>' if r['spans'] else '—'] for r in results])
    body += '<h2>Логирование</h2>' + table(
        ['Сценарий', 'Настройки', 'Записи', 'DEBUG', 'С Trace ID', 'Объём, МБ'],
        [[TITLES[r['profile']], LABELS[r['mode']], str(r['logs']['records']),
          str(r['logs']['debug_records']), str(r['logs']['correlated_records']),
          f'{r["logs"]["bytes"] / 1e6:.2f}'] for r in results])
    body += '<p>CPU — разность накопленных счётчиков контейнера. RAM — рабочая память без файлового кеша; '
    body += 'среднее взвешено по времени. Прогрев исключён. Отрицательная разница сохраняется. '
    body += 'Docker Engine и ядро ОС не входят в ресурсы контейнера monitor.</p>'
    (directory / 'index.html').write_text(page('Трассировка и логирование', body))
    for result in results:
        query = urlencode({'var-suite': directory.name, 'var-run': directory.name + '/' + result['run']})
        content = '<nav><a href="../index.html">Сравнение</a> · <a href="traces/index.html">Трейсы</a> · '
        content += f'<a href="http://localhost:3000/d/load/load?{html.escape(query)}">Grafana</a></nav>'
        content += table(['Запросы', 'RPS', 'Неожиданные ошибки'], [[str(result['requests']), f"{result['rps']:.2f}", str(result['unexpected_errors'])]])
        (directory / result['run'] / 'index.html').write_text(page(TITLES[result['profile']] + ' · ' + LABELS[result['mode']], content))
    return results


def collect(source, output):
    paths = list(source.rglob('manifest.json'))
    output.mkdir(parents=True, exist_ok=False)
    for path in paths:
        if any(p.is_symlink() for p in [path.parent, *path.parent.rglob('*')]):
            raise ValueError('Symbolic links are not allowed')
        shutil.copytree(path.parent, output / path.parent.name)
    compare(output)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--profile', choices=('all', *PROFILES), default='all')
    parser.add_argument('--smoke', action='store_true')
    parser.add_argument('--skip-build', action='store_true')
    parser.add_argument('--repeat', type=int, default=1)
    parser.add_argument('--warmup-seconds', type=int, default=60)
    parser.add_argument('--stable-vus', type=int, default=4)
    parser.add_argument('--peak-vus', type=int, default=128)
    parser.add_argument('--soak-seconds', type=int, default=600)
    parser.add_argument('--recovery-seconds', type=int, default=600)
    parser.add_argument('--application-cpus', default='0,1')
    parser.add_argument('--generator-cpus', default='2')
    parser.add_argument('--monitor-cpus', default='3')
    args = parser.parse_args()
    if args.repeat < 1:
        parser.error('repeat must be positive')
    if not args.smoke and args.warmup_seconds < 60:
        parser.error('warmup must be at least 60 seconds')
    cpus = cpu_layout(args.application_cpus, args.generator_cpus, args.monitor_cpus)
    profiles = PROFILES if args.profile == 'all' else (args.profile,)
    plans = [make_plan(p, args.smoke, args.stable_vus, min(8, args.peak_vus) if args.smoke else args.peak_vus,
                       args.soak_seconds, args.recovery_seconds) for p in profiles]
    if not args.smoke:
        for plan in plans:
            plan['phases'][0]['seconds'] = args.warmup_seconds
            plan['duration_seconds'] = sum(phase['seconds'] for phase in plan['phases'])
    info = json.loads(docker('info', '--format', '{{json .}}', capture_output=True).stdout)
    if max(int(n) for group in cpus.values() for n in group.split(',')) >= info['NCPU']:
        parser.error('Not enough Docker CPUs')
    output = ROOT / 'results' / (datetime.now(timezone.utc).strftime('%Y%m%dT%H%M%SZ') + '-' + secrets.token_hex(3))
    output.mkdir(parents=True)
    IMAGES['collector'] = 'otel/opentelemetry-collector-contrib:0.148.0'
    images = prepare_images(output, args.skip_build)
    machine = machine_info(info)
    write_json(output / 'environment.json', {'machine': machine, 'cpu_sets': cpus,
        'background_containers': docker('ps', '--format', '{{.Names}}', capture_output=True).stdout.splitlines()})
    print('Results:', output, flush=True)
    for attempt in range(args.repeat):
        modes = list(MODES)
        modes = modes[attempt % 4:] + modes[:attempt % 4]
        for plan in plans:
            for mode in modes:
                name = f'{attempt + 1:02d}-{plan["profile"]}-{mode}'
                print('Starting:', name, flush=True)
                run_one(output / name, plan, images, cpus, name, machine, MODES[mode])
                verify_telemetry(output / name, MODES[mode],
                                 json.loads((output / name / 'summary.json').read_text()))
    compare(output)
    print('Report:', output / 'index.html', flush=True)


if __name__ == '__main__':
    main()
