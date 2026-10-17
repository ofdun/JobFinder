import argparse
import gzip
import html
import json
import math
import statistics
import sys
from collections import defaultdict
from datetime import datetime
from pathlib import Path

sys.path.append(str(Path(__file__).resolve().parents[1] / 'benchmarks'))
from detail_charts import linear_histograms
from report_view import number, table


QUANTILES = (0.5, 0.75, 0.9, 0.95, 0.99)
TITLES = {'ramp': 'Рост нагрузки', 'stress': 'Перегрузка и восстановление', 'soak': 'Длительная нагрузка',
          'soak-10': 'Длительная нагрузка · 10% ошибочных действий', 'soak-20': 'Длительная нагрузка · 20% ошибочных действий'}
PHASE_TITLES = {'warmup': 'Прогрев', 'baseline': 'Обычная нагрузка', 'overload-ramp': 'Рост до перегрузки',
                'overload': 'Перегрузка', 'return-to-normal': 'Снижение нагрузки',
                'recovery': 'Восстановление', 'steady': 'Постоянная нагрузка'}


def quantile(values, probability):
    if not values or not 0 <= probability <= 1:
        raise ValueError('Nonempty values and probability in [0, 1] required')
    ordered = sorted(values)
    position = (len(ordered) - 1) * probability
    lo, hi = math.floor(position), math.ceil(position)
    return ordered[lo] + (ordered[hi] - ordered[lo]) * (position - lo)


def describe(values):
    values = [value for value in values if value is not None]
    if not values:
        return None
    return {'count': len(values), 'min': min(values), 'max': max(values), 'mean': statistics.mean(values),
            'median': statistics.median(values), 'stdev': statistics.stdev(values) if len(values) > 1 else 0,
            'percentiles': {str(p): quantile(values, p) for p in QUANTILES}}


def read_samples(path):
    samples, origin, vus = [], None, []
    with gzip.open(path, 'rt') as source:
        for line in source:
            record = json.loads(line)
            if record.get('type') != 'Point':
                continue
            data = record['data']
            metric = record['metric']
            if metric == 'load_started_ms':
                origin = data['value'] / 1000
            elif metric in ('operation_latency_ms', 'vus'):
                when = datetime.fromisoformat(data['time'].replace('Z', '+00:00')).timestamp()
                if metric == 'vus':
                    vus.append((when, data['value']))
                else:
                    samples.append({'time': when, 'latency_ms': data['value'], **(data.get('tags') or {})})
    if origin is None or not samples:
        raise ValueError('Missing load clock or operation measurements')
    return samples, origin, vus


def windows(samples, start, stop, width):
    if width <= 0 or stop <= start:
        raise ValueError('Invalid measurement interval')
    buckets = [[] for _ in range(math.ceil((stop - start) / width))]
    for sample in samples:
        if start <= sample['time'] < stop:
            buckets[min(len(buckets) - 1, int((sample['time'] - start) // width))].append(sample)
    result = []
    for index, bucket in enumerate(buckets):
        seconds = min(width, stop - start - index * width)
        good = [s for s in bucket if s.get('unexpected') == 'false' and s.get('expected_error') == 'false']
        regular = [s for s in bucket if s.get('expected_error') == 'false']
        unexpected = sum(s.get('unexpected') == 'true' for s in bucket)
        result.append({'time': start + index * width, 'seconds': seconds, 'requests': len(bucket),
                       'rps': len(bucket) / seconds, 'successful_rps': len(good) / seconds,
                       'unexpected_rate': unexpected / len(bucket) if bucket else None,
                       'expected_error_rate': sum(s.get('expected_error') == 'true' for s in bucket) / len(bucket) if bucket else None,
                       **{f'p{int(p * 100)}_ms': quantile([s['latency_ms'] for s in regular], p)
                          if regular else None for p in QUANTILES}})
    return result


def phase_ranges(plan, origin):
    result = []
    start = origin
    for phase in plan['phases']:
        result.append({**phase, 'start': start, 'end': start + phase['seconds']})
        start += phase['seconds']
    return result


def healthy(window, plan, minimum_rps=0):
    return (window['requests'] >= plan['minimum_window_requests'] and window['p95_ms'] is not None
            and window['p95_ms'] < plan['latency_limit_ms'] and window['unexpected_rate'] < plan['error_limit']
            and window['successful_rps'] >= minimum_rps)


def recovery(window_data, phases, plan):
    baseline = next(p for p in phases if p['name'] == 'baseline')
    overload = next(p for p in phases if p['name'] == 'overload')
    normal = next(p for p in phases if p['name'] == 'recovery')
    contained = lambda phase: [w for w in window_data if phase['start'] <= w['time'] and w['time'] + w['seconds'] <= phase['end'] + 0.001]
    base = contained(baseline)
    peak = contained(overload)
    base_rps = statistics.median(w['successful_rps'] for w in base) if base else 0
    if not base or not all(healthy(w, plan) for w in base):
        return {'state': 'baseline_not_stable', 'seconds': None}
    if not peak or all(healthy(w, plan) for w in peak):
        return {'state': 'overload_not_observed', 'seconds': None}
    streak = []
    for window in contained(normal):
        if healthy(window, plan, base_rps * 0.8):
            streak.append(window)
            if len(streak) == plan['recovery_windows']:
                return {'state': 'recovered', 'seconds': streak[0]['time'] - normal['start'],
                        'confirmed_after_seconds': window['time'] + window['seconds'] - normal['start'],
                        'baseline_successful_rps': base_rps}
        else:
            streak = []
    return {'state': 'not_recovered_within_observation', 'seconds': None, 'observation_seconds': normal['seconds']}


def resource_summary(records, start, stop):
    groups = defaultdict(list)
    for record in records:
        if start <= record['time'] <= stop and 'error' not in record:
            groups[record['component']].append(record)
    result = {}
    for name, rows in groups.items():
        rows.sort(key=lambda row: row['time'])
        info = {key: describe([row.get(key) for row in rows]) for key in
                ('cpu_percent', 'ram_bytes', 'disk_read_bytes_per_second', 'disk_write_bytes_per_second')}
        for key in ('disk_read_bytes', 'disk_write_bytes'):
            values = [row[key] for row in rows if row.get(key) is not None]
            info[key + '_total'] = max(values) - min(values) if values else None
        if len(rows) > 1:
            times = [row['time'] - rows[0]['time'] for row in rows]
            avg_x = statistics.mean(times)
            avg_y = statistics.mean(row['ram_bytes'] for row in rows)
            denominator = sum((x - avg_x) ** 2 for x in times)
            info['ram_bytes_per_second_slope'] = sum((x - avg_x) * (row['ram_bytes'] - avg_y)
                                                       for x, row in zip(times, rows)) / denominator if denominator else None
            info['ram_start_bytes'] = rows[0]['ram_bytes']
            info['ram_end_bytes'] = rows[-1]['ram_bytes']
        result[name] = info
    return result


def summarize(samples, origin, resources, manifest):
    plan = manifest['plan']
    phases = phase_ranges(plan, origin)
    start = next(p['start'] for p in phases if p['kind'] == 'measurement')
    stop = phases[-1]['end']
    measured = [s for s in samples if start <= s['time'] < stop]
    normal = [s for s in measured if s.get('expected_error') == 'false']
    timeline = windows(measured, start, stop, plan['window_seconds'])
    grouped = defaultdict(list)
    for sample in measured:
        grouped[sample['operation']].append(sample['latency_ms'])
    phases_summary = []
    for phase in phases:
        rows = [s for s in samples if phase['start'] <= s['time'] < phase['end']]
        good = [s['latency_ms'] for s in rows if s.get('expected_error') == 'false']
        errors = sum(s.get('unexpected') == 'true' for s in rows)
        p95 = quantile(good, 0.95) if good else None
        phases_summary.append({**phase, 'requests': len(rows), 'p95_ms': p95,
                               'unexpected_rate': errors / len(rows) if rows else None,
                               'rps': len(rows) / phase['seconds'],
                               'stable': bool(rows) and p95 is not None and p95 < plan['latency_limit_ms']
                                         and errors / len(rows) < plan['error_limit']})
    stable = [p['vus'] for p in phases_summary if p['kind'] == 'measurement' and p['plateau'] and p['stable']]
    failing = [p['vus'] for p in phases_summary if p['kind'] == 'measurement' and p['plateau'] and not p['stable']]
    observed_errors = sum(s.get('expected_error') == 'true' for s in measured) / len(measured) if measured else None
    return {'profile': plan['profile'], 'smoke': plan['smoke'], 'origin': origin,
            'measurement_start': start, 'measurement_stop': stop, 'requests': len(measured),
            'sample_sufficiency': len(measured) >= plan['minimum_samples'],
            'latency_ms': describe([s['latency_ms'] for s in normal]),
            'all_latency_ms': describe([s['latency_ms'] for s in measured]),
            'expected_error_latency_ms': describe([s['latency_ms'] for s in measured if s.get('expected_error') == 'true']),
            'unexpected_errors': sum(s.get('unexpected') == 'true' for s in measured),
            'error_fraction_target': plan['error_percent'] / 100, 'error_fraction_observed': observed_errors,
            'error_fraction_within_tolerance': observed_errors is not None and abs(observed_errors - plan['error_percent'] / 100) <= 0.01,
            'rps': describe([w['rps'] for w in timeline]), 'successful_rps': describe([w['successful_rps'] for w in timeline]),
            'operations': {key: describe(values) for key, values in grouped.items()},
            'windows': timeline, 'phases': phases_summary,
            'highest_stable_vus': max(stable) if stable else None,
            'first_failing_vus': min(failing) if failing else None,
            'recovery': recovery(timeline, phases, plan) if plan['profile'] == 'stress' else None,
            'resources': resource_summary(resources, start, stop),
            'monitor_errors': sum('error' in row and start <= row['time'] <= stop for row in resources)}


def fmt(value):
    return '—' if value is None else number(value)


def phase_title(phase):
    return PHASE_TITLES.get(phase['name'], 'Постоянная нагрузка' if phase['plateau'] else 'Рост нагрузки')


def figure(title, series, xlabel='От начала нагрузки, с', ylabel='', mode='lines', bars=None):
    series = [(name, points) for name, points in series if points]
    if not series:
        return ''
    traces = []
    for name, points in series:
        trace = dict(name=name, legendgroup=name, x=[p[0] for p in points], y=[p[1] for p in points],
                     type='bar' if bars else 'scatter')
        if bars:
            edges, _ = bars
            trace.update(width=[(b - a) * 0.94 / len(series) for a, b in zip(edges, edges[1:])],
                         customdata=list(zip(edges, edges[1:])),
                         hovertemplate='%{customdata[0]:.2f}–%{customdata[1]:.2f}<br>%{y} <extra>%{fullData.name}</extra>')
        else:
            trace.update(mode='lines', line={'width': 2}, connectgaps=False,
                         hovertemplate='%{y:.2f}<extra>%{fullData.name}</extra>')
            if mode == 'percentiles':
                trace['hovertemplate'] = 'p%{x:.1f}: %{y:.2f}<extra>%{fullData.name}</extra>'
        traces.append(trace)
        if mode == 'percentiles':
            selected = [(x, y) for x, y in points if x in [p * 100 for p in QUANTILES]]
            traces.append(dict(name=name, legendgroup=name, type='scatter', mode='markers', showlegend=False,
                               x=[p[0] for p in selected], y=[p[1] for p in selected], marker={'size': 8},
                               hovertemplate='p%{x}: %{y:.2f}<extra>%{fullData.name}</extra>'))
    payload = json.dumps(dict(data=traces, xlabel=xlabel, ylabel=ylabel, mode=mode),
                         ensure_ascii=False, allow_nan=False).replace('<', '\\u003c')
    return (f'<section class="card"><h2>{html.escape(title)}</h2>'
            f'<div class="interactive-chart" role="img" aria-label="{html.escape(title)}"></div>'
            f'<script type="application/json" class="chart-data">{payload}</script></section>')


def distribution(title, series, unit):
    series = [(name, sorted(values)) for name, values in series if values]
    if not series:
        return ''
    probabilities = sorted({i / 1000 for i in range(1001)} | set(QUANTILES))
    curves = []
    for name, values in series:
        points = []
        for p in probabilities:
            position = (len(values) - 1) * p
            lo, hi = math.floor(position), math.ceil(position)
            value = values[lo] + (values[hi] - values[lo]) * (position - lo)
            points.append((p * 100, value))
        curves.append((name, points))
    body = figure('Перцентили · ' + title, curves, 'Перцентиль, %', unit, mode='percentiles')
    edges, counts = linear_histograms([values for _, values in series])
    body += figure('Гистограмма · ' + title,
                   [(name, [((edges[i] + edges[i + 1]) / 2, count) for i, count in enumerate(histogram)])
                    for (name, _), histogram in zip(series, counts)], unit,
                   'Количество запросов' if unit == 'Задержка, мс' else 'Количество окон', bars=(edges, counts))
    return body


def page(title, body):
    css = (Path(__file__).resolve().parents[1] / 'benchmarks/report.css').read_text().replace('svg{display:block;width:100%;height:auto}', '')
    css += '\n.interactive-chart{width:100%;height:400px;min-width:0}nav{margin-bottom:20px}'
    script = (Path(__file__).with_name('report.js')).read_text()
    return ('<!doctype html><html lang="ru"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">'
            f'<title>{html.escape(title)}</title><style>{css}</style><main><header><h1>{html.escape(title)}</h1></header>'
            + body + '</main><script src="https://cdn.plot.ly/plotly-4.1.1.min.js" charset="utf-8"></script>'
            + f'<script>{script}</script></html>')


def render(summary, samples, vus, resources, manifest):
    origin = summary['origin']
    plan = manifest['plan']
    rows = []
    for name, key in [('Задержка всех запросов, мс', 'all_latency_ms'),
                      ('Задержка штатных запросов, мс', 'latency_ms'), ('Задержка ошибочных действий, мс', 'expected_error_latency_ms'),
                      ('RPS', 'rps'), ('Успешные штатные запросы/с', 'successful_rps')]:
        info = summary[key]
        if info:
            rows.append([name, str(info['count']), fmt(info['mean']), fmt(info['min']), fmt(info['max']),
                         *[fmt(info['percentiles'][str(p)]) for p in QUANTILES]])
    body = '<div class="run-meta">SMOKE</div>' if summary['smoke'] else '<div class="run-meta">Полный</div>'
    body += '<nav><a href="../index.html">Все прогоны</a><a href="summary.json">Статистика</a><a href="manifest.json">Окружение</a></nav>'
    body += f'<div class="kpis"><div><strong>{summary["requests"]}</strong><span>запросов</span></div>'
    body += f'<div><strong>{summary["unexpected_errors"]}</strong><span>неожиданных ошибок</span></div>'
    fraction = summary['error_fraction_observed']
    body += f'<div><strong>{fmt(fraction * 100 if fraction is not None else None)}%</strong><span>ошибочных действий</span></div>'
    body += f'<div><strong>{fmt(summary["measurement_stop"] - summary["measurement_start"])}</strong><span>секунд измерения</span></div></div>'
    body += '<section class="card"><h2>Статистика</h2>' + table(['Метрика', 'Выборка', 'Среднее', 'Min', 'Max', 'p50', 'p75', 'p90', 'p95', 'p99'], rows) + '</section>'
    if not summary['sample_sufficiency']:
        body += '<div class="warning">Недостаточно запросов для полного исследования</div>'
    phases = [[phase_title(p), str(p['vus']), str(p['seconds']), str(p['requests']), fmt(p['p95_ms']),
               fmt(p['unexpected_rate'] * 100 if p['unexpected_rate'] is not None else None),
               'да' if p['stable'] else 'нет'] for p in summary['phases']]
    body += '<section class="card"><h2>Ступени нагрузки</h2>' + table(['Этап', 'Пользователи', 'с', 'Запросы', 'p95, мс', 'Отказы, %', 'Пороги соблюдены'], phases) + '</section>'
    if summary['profile'] == 'ramp':
        body += '<section class="card"><h2>Граница устойчивой работы</h2>' + table(['Устойчиво, пользователей', 'Первое превышение порогов, пользователей'],
            [[fmt(summary['highest_stable_vus']), fmt(summary['first_failing_vus'])]]) + '</section>'
    if summary['recovery']:
        info = summary['recovery']
        labels = {'recovered': 'Восстановление подтверждено', 'baseline_not_stable': 'Исходная нагрузка нестабильна',
                  'overload_not_observed': 'Перегрузка не достигнута', 'not_recovered_within_observation': 'Не восстановилось за время наблюдения'}
        elapsed = ('&gt; ' + fmt(info['observation_seconds'])
                   if info['state'] == 'not_recovered_within_observation' else fmt(info['seconds']))
        body += '<section class="card"><h2>Восстановление</h2>' + table(['Результат', 'Время до первого устойчивого окна, с', 'Подтверждено через, с'],
            [[labels[info['state']], elapsed, fmt(info.get('confirmed_after_seconds'))]]) + '</section>'
    points = summary['windows']
    body += figure('Задержка штатных запросов во времени',
                   [(f'p{int(p * 100)}', [(w['time'] - origin, w[f'p{int(p * 100)}_ms']) for w in points])
                    for p in QUANTILES], ylabel='Задержка, мс')
    body += figure('Пропускная способность', [('Все запросы', [(w['time'] - origin, w['rps']) for w in points]),
                                             ('Успешные', [(w['time'] - origin, w['successful_rps']) for w in points])], ylabel='Запросов / с')
    body += figure('Неожиданные отказы', [('Отказы', [(w['time'] - origin, 100 * w['unexpected_rate'])
                                                    for w in points if w['unexpected_rate'] is not None])], ylabel='Отказы, %')
    body += figure('Виртуальные пользователи', [('Пользователи', [(t - origin, value) for t, value in vus])], ylabel='Активных пользователей')
    values = sorted(s['latency_ms'] for s in samples
                    if summary['measurement_start'] <= s['time'] < summary['measurement_stop'])
    body += distribution('задержка всех запросов', [('Запросы', values)], 'Задержка, мс')
    body += distribution('пропускная способность', [('Все запросы', [w['rps'] for w in points]),
                                                  ('Успешные', [w['successful_rps'] for w in points])], 'Запросов / с')
    names = sorted(summary['resources'], key=lambda name: (name != 'backend', name))
    body += '<section class="card"><h2>Ресурсы компонентов</h2><label>Компонент <select id="component">'
    body += ''.join(f'<option value="{html.escape(name)}">{html.escape(name)}</option>' for name in names) + '</select></label></section>'
    for name in names:
        info = summary['resources'][name]
        items = [r for r in resources if r['component'] == name and 'error' not in r
                 and summary['measurement_start'] <= r['time'] <= summary['measurement_stop']]
        definitions = [('CPU, % одного ядра', 'cpu_percent', 1), ('RAM, МиБ', 'ram_bytes', 2 ** 20),
                       ('Чтение диска, КиБ/с', 'disk_read_bytes_per_second', 1024), ('Запись диска, КиБ/с', 'disk_write_bytes_per_second', 1024)]
        rows = [[label, *[fmt(info[key][field] / scale) if info[key] else 'нет данных' for field in ('min', 'max', 'median', 'mean')]]
                for label, key, scale in definitions]
        body += f'<section data-component="{name}"><section class="card"><h2>{name}</h2>' + table(['Ресурс', 'Min', 'Max', 'Медиана', 'Среднее'], rows) + '</section>'
        for label, key, scale in definitions:
            body += figure(label, [(name, [(r['time'] - origin, r[key] / scale) for r in items if r.get(key) is not None])], ylabel=label)
        body += '</section>'
    body += '<section class="card"><h2>Параметры измерения</h2>' + table(['Параметр', 'Значение'], [
        ['Окно RPS, с', str(plan['window_seconds'])], ['Нулевые окна', 'учтены'],
        ['Задержка', 'отправка + ожидание + получение HTTP-ответа'],
        ['Учёт запросов', 'по завершению'], ['Прогрев', 'исключён'],
        ['Порог p95, мс', '&lt; ' + str(plan['latency_limit_ms'])],
        ['Порог неожиданных отказов, %', '&lt; ' + str(plan['error_limit'] * 100)],
        ['Ошибочные действия: план / факт, %', f'{plan["error_percent"]} / {fmt(fraction * 100 if fraction is not None else None)}'],
        ['Ошибки мониторинга', str(summary['monitor_errors'])], ['GPU', 'не используется'],
        *[['CPU · ' + name, str(value)] for name, value in manifest.get('cpu_sets', {}).items()]
    ]) + '</section>'
    return page(TITLES[summary['profile']], body)


def generate(directory):
    directory = Path(directory)
    manifest = json.loads((directory / 'manifest.json').read_text())
    samples, origin, vus = read_samples(directory / 'samples.gz')
    resources = [json.loads(line) for line in (directory / 'resources.jsonl').read_text().splitlines()]
    summary = summarize(samples, origin, resources, manifest)
    (directory / 'summary.json').write_text(json.dumps(summary, ensure_ascii=False, indent=2) + '\n')
    (directory / 'index.html').write_text(render(summary, samples, vus, resources, manifest))
    return summary


def validate_run_conditions(manifests):
    images = {'backend': set(), 'generator': set()}
    reference = None
    for manifest in manifests:
        if manifest['status'] != 'complete':
            raise ValueError('Incomplete run in comparison')
        for component, seen in images.items():
            image = manifest['containers'][component]['image']
            if image in seen:
                raise ValueError('Each run must use distinct application and generator images')
            seen.add(image)
        conditions = {key: manifest[key] for key in ('base_images', 'application_settings', 'cpu_sets')}
        conditions['limits'] = {name: data['resources'] for name, data in manifest['containers'].items()}
        if reference is not None and conditions != reference:
            raise ValueError('Runs must use the same base images, application settings and resource limits')
        reference = conditions


def generate_suite(directory):
    directory = Path(directory)
    rows, manifests = [], []
    for path in sorted(directory.iterdir()):
        if not (path / 'summary.json').exists():
            continue
        summary = json.loads((path / 'summary.json').read_text())
        manifest = json.loads((path / 'manifest.json').read_text())
        manifests.append(manifest)
        rows.append([f'<a href="{path.name}/index.html">{TITLES[summary["profile"]]}</a>', 'SMOKE' if summary['smoke'] else 'Полный',
                     fmt(summary['measurement_stop'] - summary['measurement_start']),
                     str(summary['requests']), fmt(summary['latency_ms']['percentiles']['0.95'] if summary['latency_ms'] else None),
                     fmt(summary['rps']['mean']), str(summary['unexpected_errors'])])
    validate_run_conditions(manifests)
    (directory / 'index.html').write_text(page('Нагрузочное тестирование JobFinder', table(['Прогон', 'Режим', 'Измерение, с', 'Запросы', 'p95, мс', 'Средний RPS', 'Отказы'], rows)))


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('directory', type=Path)
    args = parser.parse_args()
    if (args.directory / 'manifest.json').exists():
        generate(args.directory)
    else:
        for child in args.directory.iterdir():
            if (child / 'samples.gz').exists():
                generate(child)
        generate_suite(args.directory)
