import html
from pathlib import Path

from detail_charts import charts
from report_view import COLORS, linear_ticks, metrics, number, phases


ORDERS = {"random": "Случайный порядок", "ascending": "Зарплаты по возрастанию",
          "descending": "Зарплаты по убыванию"}
VARIANTS = ("sort", "heap")
NAMES = ("Сортировка", "Top-K")
PERCENTILES = {"p50": "0.5", "p75": "0.75", "p90": "0.9", "p95": "0.95", "p99": "0.99"}


def limit_value(mode, size):
    return size // 2 if mode == "half" else int(mode)


def stats_for(stats, order, size, mode):
    limit = limit_value(mode, size)
    return [stats[f"{order}/{variant}/{size}/{limit}"] for variant in VARIANTS]


def overview(stats, size, mode, orders, percentile):
    values = [[info["percentiles_ns"][PERCENTILES[percentile]] / 1000 for info in stats_for(stats, order, size, mode)]
              for order in orders]
    upper = max(info["percentiles_ns"]["0.99"] / 1000
                for order in orders for info in stats_for(stats, order, size, mode))
    ticks = linear_ticks(max(0.001, upper) * 1.2)
    maximum = ticks[-1]
    parts = ['<div class="overview-chart"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 940 270" '
             f'role="img" aria-label="Сравнение {percentile}"><title>Сравнение {percentile}, мкс</title>'
             '<style>text{font-family:Arial,sans-serif;font-size:14px;fill:#334155}</style>']
    for tick in ticks:
        x = 250 + 630 * tick / maximum
        parts.append(f'<path d="M{x},20 V225" stroke="#e2e8f0"/>'
                     f'<text x="{x}" y="250" text-anchor="middle">{number(tick)}</text>')
    for row, (order, pair) in enumerate(zip(orders, values)):
        parts.append(f'<text x="12" y="{51+row*68}">{ORDERS[order]}</text>')
        for index, value in enumerate(pair):
            width = 630 * value / maximum
            top = 27 + row * 68 + index * 23
            parts.append(f'<rect x="250" y="{top}" width="{width:.2f}" height="17" rx="3" fill="{COLORS[index]}"/>'
                         f'<text x="{257+width:.2f}" y="{top+14}">{number(value)}</text>')
    return ''.join(parts) + '</svg></div>'


def percentile_buttons():
    return ('<div class="chart-buttons" role="group" aria-label="Перцентиль">'
            + ''.join(f'<button type="button" data-chart="{name}" aria-pressed="{str(index == 0).lower()}">{name}</button>'
                      for index, name in enumerate(PERCENTILES)) + '</div>')


def render(manifest, datasets, diagnostics=False):
    root = Path(__file__).parent
    config = manifest["configuration"]
    modes = config["limits"]
    orders = config["orders"]
    mode_names = {mode: "N / 2" if mode == "half" else mode for mode in modes}
    options = lambda pairs: ''.join(f'<option value="{value}">{label}</option>' for value, label in pairs)
    title = "Графики и фазы замеров" if diagnostics else "Отбор лучших вакансий"
    content = ['<!doctype html><html lang="ru"><head><meta charset="utf-8">'
               '<meta name="viewport" content="width=device-width,initial-scale=1">'
               f'<title>{title} — микробенчмарки</title><style>',
               (root / 'report.css').read_text(), '</style></head><body><main>',
               '<header><div><span class="eyebrow">JOBFINDER · MICROBENCHMARKS</span>'
               f'<h1>{title}</h1></div>'
               '<nav><a href="summary.json">JSON</a><a href="manifest.json">Окружение</a></nav></header>',
               '<div class="toolbar"><label>Прогон <select id="run-selector">'
               + options((run["number"], run["number"]) for run, _, _ in datasets) + '</select></label>'
               '<label>Вакансий <select id="size-selector">'
               + options((size, number(size)) for size in config["sizes"]) + '</select></label>'
               '<label>Выбрать <select id="limit-selector">'
               + options(mode_names.items()) + '</select></label>'
               '<span class="legend loop">Сортировка</span><span class="legend stream">Top-K</span></div>',
               f'<div class="kpis"><div><strong>{config["trials"]}</strong><span>испытаний</span></div>'
               f'<div><strong>{number(config["trials"]*config["samples"])}</strong><span>измерений</span></div>'
               '<div><strong>1 CPU</strong><span>на контейнер</span></div>'
               f'<div><strong>{number(manifest["wall_seconds"])} с</strong><span>весь запуск, со сборкой</span></div></div>']
    if manifest['smoke']:
        content.append('<div class="warning">SMOKE CHECK ONLY · НЕДОСТАТОЧНО ИСПЫТАНИЙ</div>')
    for run, cases, stats in datasets:
        content.append(f'<article data-run="{run["number"]}" aria-label="Прогон {run["number"]}">'
                       f'<div class="run-meta"><span>Прогон {run["number"]} · контейнер {number(run["wall_seconds"])} с</span>'
                       f'<a href="{html.escape(run["raw_file"])}">Сырые измерения · CSV</a></div>')
        for mode in modes:
            content.append(f'<section data-limit="{mode}">')
            for size in config['sizes']:
                limit = limit_value(mode, size)
                content.append(f'<section data-size="{size}"><div class="chart-group percentile-group">'
                               + percentile_buttons())
                for percentile in PERCENTILES:
                    content.append(f'<section class="card percentile-comparison" data-percentile="{percentile}" data-chart-panel="{percentile}">'
                                   f'<h2>Сравнение {percentile}'
                                   f' <span>мкс · N = {number(size)} · K = {number(limit)}</span></h2>'
                                   + overview(stats, size, mode, orders, percentile) + '</section>')
                content.append('</div><details class="case detailed-statistics" open><summary>Подробная статистика</summary>'
                               '<div class="case-content">')
                for order in orders:
                    infos = stats_for(stats, order, size, mode)
                    if diagnostics:
                        pair = [cases[(order, variant, size, limit)] for variant in VARIANTS]
                        content.append(f'<details class="case"><summary>{ORDERS[order]}'
                                       '<span>Графики и статистика</span></summary>'
                                       '<div class="case-content"><h3>Задержка, мкс</h3>'
                                       + metrics(infos, NAMES) + charts(pair, NAMES) + phases(infos, NAMES)
                                       + '</div></details>')
                    else:
                        content.append(f'<section class="scenario-numbers"><h3>{ORDERS[order]} · мкс</h3>'
                                       + metrics(infos, NAMES) + '</section>')
                content.append('</div></details></section>')
            content.append('</section>')
        baseline = stats['overhead/harness/0/0']
        content.append('<section class="card overhead"><h2>Накладные расходы</h2>'
                       '<h3>Задержка, мкс</h3>'
                       + metrics([baseline], ['Harness'])
                       + phases([baseline], ['Harness']) + '</section></article>')
    content.append('<footer>p50 · p75 · p90 · p95 · p99 / индивидуальные операции · без коррекции overhead</footer>')
    content.append('</main><script>' + (root / 'report.js').read_text() + '</script></body></html>')
    return '\n'.join(content)
