import html
import math
import statistics
from collections import defaultdict
from pathlib import Path


COLORS = ("#2563eb", "#d97706")
NAMES = ("Цикл", "Streams")
SCENARIOS = {"filter": "Фильтрация ID", "sum": "Сумма зарплат", "best": "Максимальная зарплата"}
PHASES = {"setup": "Setup", "warmup": "Warmup", "measurement": "Измерение", "teardown": "Teardown"}


def number(value):
    if value == 0:
        return "0"
    if abs(value) < 0.01:
        return f"{value:.3g}"
    return f"{value:,.2f}".rstrip("0").rstrip(".").replace(",", " ")


def table(headers, rows):
    return ('<div class="table-scroll"><table><thead><tr>'
            + ''.join(f'<th scope="col">{h}</th>' for h in headers)
            + '</tr></thead><tbody>'
            + ''.join('<tr>' + ''.join(f'<td>{cell}</td>' for cell in row) + '</tr>' for row in rows)
            + '</tbody></table></div>')


def logarithmic_ticks(low, high):
    ticks = [factor * 10 ** power for power in range(math.floor(math.log10(low)), math.ceil(math.log10(high)) + 1)
             for factor in (1, 2, 5) if low <= factor * 10 ** power <= high]
    if len(ticks) > 7:
        ticks = ticks[::math.ceil(len(ticks) / 7)]
    if len(ticks) < 3:
        step = (high - low) / 4
        magnitude = 10 ** math.floor(math.log10(step))
        step = next(factor * magnitude for factor in (1, 2, 2.5, 5, 10) if factor * magnitude >= step)
        ticks = [step * i for i in range(math.ceil(low / step), math.floor(high / step) + 1)]
    return ticks or [low, high]


def linear_ticks(high, integer=False):
    step = max(high / 4, 1 if integer else 0.000001)
    magnitude = 10 ** math.floor(math.log10(step))
    step = next(factor * magnitude for factor in (1, 2, 2.5, 5, 10) if factor * magnitude >= step)
    if integer:
        step = math.ceil(step)
    return [step * i for i in range(math.ceil(high / step) + 1)]


def shared_histograms(groups, bins=24):
    values = [value for group in groups for value in group]
    low, high = math.log10(max(1, min(values))), math.log10(max(1, max(values)))
    high = max(high, low + 0.1)
    edges = [10 ** (low + i * (high - low) / bins) / 1000 for i in range(bins + 1)]
    counts = []
    for group in groups:
        frequencies = [0] * bins
        for value in group:
            index = min(bins - 1, int((math.log10(max(1, value)) - low) / (high - low) * bins))
            frequencies[index] += 1
        counts.append(frequencies)
    return edges, counts


def chart(series, title, xlabel, ylabel, log_x=False, log_y=False, bars=None):
    width, height = 800, 310
    left, right, top, bottom = 86, 26, 40, 58
    xs = [point[0] for _, points in series for point in points]
    ys = [point[1] for _, points in series for point in points]
    xmin, xmax = (bars[0][0], bars[0][-1]) if bars else (min(xs), max(xs))
    ymin, ymax = (max(0.001, min(ys)), max(ys)) if log_y else (0, max(ys))
    xmax = max(xmax, xmin * 1.1 if log_x else xmin + 0.001)
    ymax = max(ymax * 1.08, ymin * 1.2 if log_y else 0.001)
    yticks = logarithmic_ticks(ymin, ymax) if log_y else linear_ticks(ymax, integer=bars is not None)
    if not log_y:
        ymax = yticks[-1]
    if not log_x:
        xmin = 0
    xticks = logarithmic_ticks(xmin, xmax) if log_x else linear_ticks(xmax)
    if not log_x:
        xmax = xticks[-1]

    def scale(value, low, high, logarithmic):
        if logarithmic:
            return (math.log10(max(low, value)) - math.log10(low)) / (math.log10(high) - math.log10(low))
        return (value - low) / (high - low)

    def x(value):
        return left + scale(value, xmin, xmax, log_x) * (width - left - right)

    def y(value):
        return height - bottom - scale(value, ymin, ymax, log_y) * (height - top - bottom)

    parts = [f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {width} {height}" role="img" '
             f'aria-label="{html.escape(title)}"><title>{html.escape(title)}</title>',
             '<style>text{font-family:Arial,sans-serif;font-size:13px;fill:#475569}</style>']
    for tick in yticks:
        parts.append(f'<path d="M{left},{y(tick):.2f} H{width-right}" stroke="#e2e8f0"/>')
        parts.append(f'<text x="{left-10}" y="{y(tick)+4:.2f}" text-anchor="end">{number(tick)}</text>')
    for tick in xticks:
        parts.append(f'<text x="{x(tick):.2f}" y="{height-bottom+24}" text-anchor="middle">{number(tick)}</text>')
    for index, (name, points) in enumerate(series):
        color = COLORS[index]
        parts.append(f'<path d="M{left+index*145},17 h22" stroke="{color}" stroke-width="3"/>')
        parts.append(f'<text x="{left+index*145+30}" y="21">{html.escape(name)}</text>')
        if bars:
            edges, counts = bars
            for position, count in enumerate(counts[index]):
                full = x(edges[position+1]) - x(edges[position])
                barwidth = full * 0.84 / len(series)
                bx = x(edges[position]) + full * 0.08 + index * barwidth
                parts.append(f'<rect x="{bx:.2f}" y="{y(count):.2f}" width="{barwidth:.2f}" '
                             f'height="{height-bottom-y(count):.2f}" fill="{color}"><title>'
                             f'{html.escape(name)}: {number(edges[position])}–{number(edges[position+1])} мкс; {count} операций'
                             '</title></rect>')
        else:
            coordinates = ' '.join(f'{x(a):.2f},{y(b):.2f}' for a, b in points)
            parts.append(f'<polyline points="{coordinates}" fill="none" stroke="{color}" stroke-width="2.2"/>')
            if len(points) <= 10:
                for a, b in points:
                    parts.append(f'<circle cx="{x(a):.2f}" cy="{y(b):.2f}" r="3.5" fill="{color}">'
                                 f'<title>{html.escape(name)}: {number(a)} / {number(b)}</title></circle>')
    parts.append(f'<text x="{(left+width-right)/2}" y="{height-9}" text-anchor="middle">{html.escape(xlabel)}</text>')
    parts.append(f'<text transform="translate(18,{(top+height-bottom)/2}) rotate(-90)" text-anchor="middle">{html.escape(ylabel)}</text></svg>')
    return ''.join(parts)


def comparison_chart(stats, size):
    maximum = max(stats[f'{s}/{v}/{size}']['percentiles_ns']['0.5'] / 1000
                  for s in SCENARIOS for v in ('loop', 'stream')) * 1.18
    parts = ['<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 940 270" role="img" aria-label="Сравнение p50">'
             '<title>Сравнение p50, мкс</title><style>text{font-family:Arial,sans-serif;font-size:14px;fill:#334155}</style>']
    for tick in range(5):
        x = 235 + 650 * tick / 4
        parts.append(f'<path d="M{x},20 V225" stroke="#e2e8f0"/><text x="{x}" y="250" text-anchor="middle">{number(maximum*tick/4)}</text>')
    for row, (scenario, label) in enumerate(SCENARIOS.items()):
        parts.append(f'<text x="12" y="{51+row*68}">{label}</text>')
        for index, implementation in enumerate(('loop', 'stream')):
            value = stats[f'{scenario}/{implementation}/{size}']['percentiles_ns']['0.5'] / 1000
            barwidth = 650 * value / maximum if maximum else 0
            top = 27 + row * 68 + index * 23
            parts.append(f'<rect x="235" y="{top}" width="{barwidth:.2f}" height="17" rx="3" fill="{COLORS[index]}"/>'
                         f'<text x="{242+barwidth:.2f}" y="{top+14}">{number(value)}</text>')
    return ''.join(parts) + '</svg>'


def charts(cases, names, kinds=None):
    times, distributions, groups = [], [], []
    for case, name in zip(cases, names):
        samples = case['samples']
        groups.append([s['latency_ns'] for s in samples])
        blocks = defaultdict(list)
        origin = min(s['elapsed_ns'] for s in samples)
        for sample in samples:
            blocks[sample['trial']].append(sample)
        times.append((name, [((statistics.mean(s['elapsed_ns'] for s in block) - origin) / 1e6,
                             statistics.mean(s['latency_ns'] for s in block) / 1000)
                            for _, block in sorted(blocks.items())]))
        ordered = sorted(groups[-1])
        indices = sorted(set(range(0, len(ordered), max(1, len(ordered)//400))) | {len(ordered)-1})
        distributions.append((name, [(100*(i+1)/len(ordered), max(1, ordered[i])/1000) for i in indices]))
    edges, counts = shared_histograms(groups)
    bars = [(name, [((edges[i]*edges[i+1])**0.5, count) for i, count in enumerate(frequencies)])
            for name, frequencies in zip(names, counts)]
    definitions = [
        ('time', 'Время', 'Время · среднее по испытанию',
         chart(times, 'Время · среднее по испытанию', 'От начала измерения алгоритма, мс', 'Задержка, мкс')),
        ('percentiles', 'Перцентили', 'Распределение по операциям',
         chart(distributions, 'Распределение по операциям', 'Доля операций, %', 'Задержка, мкс · лог. шкала', log_y=True)),
        ('histogram', 'Гистограмма', 'Гистограмма · все операции',
         chart(bars, 'Гистограмма · все операции', 'Задержка, мкс · лог. шкала', 'Количество операций', log_x=True, bars=(edges, counts))),
    ]
    if kinds is not None:
        definitions = [definition for definition in definitions if definition[0] in kinds]
    return ('<div class="chart-group">'
            + ''.join(f'<figure data-chart-panel="{key}"><figcaption>{title}</figcaption>{svg}</figure>'
                      for key, _, title, svg in definitions) + '</div>')


def metrics(infos, names):
    rows = []
    for info, name in zip(infos, names):
        values = [info['mean_ns'], info['stdev_ns'], *info['percentiles_ns'].values()]
        rows.append([name, f'{info["operations"]:,}'.replace(',', ' '), *[number(v/1000) for v in values]])
    return table(['Алгоритм', 'Операции', 'Среднее, мкс', 'SD, мкс', 'p50', 'p75', 'p90', 'p95', 'p99'], rows)


def phases(infos, names):
    rows = [[name, PHASES[phase], f'{data["operations"]:,}'.replace(',', ' '), number(data['duration_ns']/1e6)]
            for info, name in zip(infos, names) for phase, data in info['phases'].items()]
    return ('<section class="phase-statistics"><h3>Фазы и счётчики</h3>'
            + table(['Алгоритм', 'Фаза', 'Операции', 'Время, мс'], rows)
            + '</section>')


def render(manifest, datasets):
    root = Path(__file__).parent
    config = manifest['configuration']
    run_options = ''.join(f'<option value="{run["number"]}">{run["number"]}</option>' for run, _, _ in datasets)
    content = ['<!doctype html><html lang="ru"><head><meta charset="utf-8">'
               '<meta name="viewport" content="width=device-width,initial-scale=1">'
               '<title>Циклы / Streams — микробенчмарки</title><style>',
               (root/'report.css').read_text(), '</style></head><body><main>',
               '<header><div><span class="eyebrow">JOBFINDER · MICROBENCHMARKS</span><h1>Циклы / Streams</h1></div>'
               '<nav><a href="summary.json">JSON</a><a href="manifest.json">Окружение</a></nav></header>',
               f'<div class="toolbar"><label>Прогон <select id="run-selector">{run_options}</select></label>'
               '<label>Вакансий <select id="size-selector"><option>512</option><option>4096</option></select></label>'
               '<span class="legend loop">Цикл</span><span class="legend stream">Streams</span></div>',
               f'<div class="kpis"><div><strong>{config["trials"]}</strong><span>испытаний</span></div>'
               f'<div><strong>{number(config["trials"]*config["samples"])}</strong><span>измерений</span></div>'
               f'<div><strong>1 CPU</strong><span>на контейнер</span></div>'
               f'<div><strong>{number(manifest["wall_seconds"])} с</strong><span>весь запуск, со сборкой</span></div></div>']
    if manifest['smoke']:
        content.append('<div class="warning">SMOKE CHECK ONLY · НЕДОСТАТОЧНО ИСПЫТАНИЙ</div>')
    for run, cases, stats in datasets:
        content.append(f'<article data-run="{run["number"]}" aria-label="Прогон {run["number"]}">'
                       f'<div class="run-meta"><span>Прогон {run["number"]} · контейнер {number(run["wall_seconds"])} с</span>'
                       f'<a href="{html.escape(run["raw_file"])}">Сырые измерения · CSV</a></div>')
        for size in (512, 4096):
            content.append(f'<section data-size="{size}"><section class="card"><h2>Сравнение p50 <span>мкс</span></h2>')
            content.append('<div class="overview-chart">' + comparison_chart(stats, size) + '</div>')
            rows = []
            for scenario, name in SCENARIOS.items():
                loop, stream = [stats[f'{scenario}/{v}/{size}']['percentiles_ns']['0.5'] for v in ('loop', 'stream')]
                ratio = f'{stream/loop:.2f}×' if loop else '—'
                rows.append([name, number(loop/1000), number(stream/1000), ratio])
            content.append(table(['Операция', 'Цикл, мкс', 'Streams, мкс', 'Streams / цикл'], rows) + '</section>')
            for scenario, name in SCENARIOS.items():
                infos = [stats[f'{scenario}/{v}/{size}'] for v in ('loop', 'stream')]
                pair = [cases[(scenario, v, size)] for v in ('loop', 'stream')]
                content.append(f'<details class="case"><summary>{name}<span>Графики и статистика</span></summary>'
                               '<div class="case-content"><h3>Задержка, мкс</h3>' + metrics(infos, NAMES)
                               + charts(pair, NAMES) + phases(infos, NAMES) + '</div></details>')
            content.append('</section>')
        baseline = stats['overhead/harness/0']
        overhead = baseline['percentiles_ns']['0.5']
        content.append('<details class="case overhead"><summary>Накладные расходы'
                       f'<span>p50 {number(overhead)} нс · ориентир ≤5 нс: {"да" if overhead <= 5 else "НЕ ДОСТИГНУТ"}</span></summary>'
                       '<div class="case-content"><h3>Задержка, мкс</h3>'
                       + metrics([baseline], ['Harness']) + charts([cases[('overhead', 'harness', 0)]], ['Harness'])
                       + phases([baseline], ['Harness']) + '</div></details></article>')
    content.append('<footer>p50 · p75 · p90 · p95 · p99 / индивидуальные операции · без коррекции overhead</footer>')
    content.append('</main><script>' + (root/'report.js').read_text() + '</script></body></html>')
    return '\n'.join(content)
