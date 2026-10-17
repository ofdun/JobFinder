import html
import statistics
from collections import defaultdict

from report_view import COLORS, linear_ticks, number


def duration_unit(values):
    maximum = max(values)
    if maximum >= 1_000_000:
        return 1_000_000, "мс"
    if maximum >= 1_000:
        return 1_000, "мкс"
    return 1, "нс"


def linear_histograms(groups, bins=24):
    if not groups or any(not group for group in groups) or bins < 1:
        raise ValueError("Nonempty groups and positive bins required")
    maximum = max(max(group) for group in groups)
    if min(min(group) for group in groups) < 0:
        raise ValueError("Nonnegative timings required")
    high = max(1, maximum)
    edges = [high * i / bins for i in range(bins + 1)]
    counts = []
    for group in groups:
        row = [0] * bins
        for value in group:
            row[min(bins - 1, int(value / high * bins))] += 1
        counts.append(row)
    return edges, counts


def prepare(cases, names):
    groups = [[sample["latency_ns"] for sample in case["samples"]] for case in cases]
    divisor, unit = duration_unit([value for group in groups for value in group])
    trials, times, distributions = [], [], []
    elapsed = max(max(s["elapsed_ns"] for s in case["samples"]) - min(s["elapsed_ns"] for s in case["samples"])
                  for case in cases)
    time_divisor, time_unit = (1e9, "с") if elapsed >= 1e9 else (1e6, "мс")
    for case, name, values in zip(cases, names, groups):
        blocks = defaultdict(list)
        origin = min(sample["elapsed_ns"] for sample in case["samples"])
        for sample in case["samples"]:
            blocks[sample["trial"]].append(sample)
        trial_points, time_points = [], []
        for trial, block in sorted(blocks.items()):
            mean = statistics.mean(s["latency_ns"] for s in block) / divisor
            trial_points.append((trial + 1, mean))
            time_points.append(((statistics.mean(s["elapsed_ns"] for s in block) - origin) / time_divisor, mean))
        trials.append((name, trial_points))
        times.append((name, time_points))
        points = [(0, 0)]
        for index, value in enumerate(sorted(values)):
            points.extend([(value / divisor, 100 * index / len(values)),
                           (value / divisor, 100 * (index + 1) / len(values))])
        distributions.append((name, points))
    edges, counts = linear_histograms(groups)
    return {"unit": unit, "time_unit": time_unit, "trials": trials, "times": times,
            "distributions": distributions, "edges": [edge / divisor for edge in edges], "counts": counts}


def plot(series, title, xlabel, ylabel, mode="line", bars=None):
    width, height = 940, 340
    left, right, top, bottom = 68, 28, 60, 58
    xs = [x for _, points in series for x, _ in points]
    ys = [y for _, points in series for _, y in points]
    xmax = max(max(xs), 0.001)
    yticks = [0, 25, 50, 75, 100] if mode == "cdf" else linear_ticks(max(max(ys) * 1.01, 0.001), integer=bool(bars))
    ymax = yticks[-1]
    if mode == "trial":
        xticks = sorted(set([1, *[value for value in (25, 50, 75, 100) if value < xmax], xmax]))
        xmin = 1 if xmax > 1 else 0
    else:
        xmin = 0
        xticks = linear_ticks(xmax)
        xmax = xticks[-1]
    if bars:
        xmax = max(xmax, bars[0][-1])

    def x(value):
        return left + (value - xmin) / (xmax - xmin) * (width - left - right)

    def y(value):
        return height - bottom - value / ymax * (height - top - bottom)

    parts = [f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {width} {height}" role="img" '
             f'aria-label="{html.escape(title)}"><title>{html.escape(title)}</title>'
             '<style>text{font-family:Arial,sans-serif;font-size:13px;fill:#64748b}</style>',
             f'<text x="{left}" y="24" style="font-weight:600;fill:#334155">{html.escape(ylabel)}</text>']
    for index, (name, _) in enumerate(series):
        lx = width - right - (len(series) - index) * 150
        parts.append(f'<rect x="{lx}" y="13" width="10" height="10" rx="3" fill="{COLORS[index]}"/>'
                     f'<text x="{lx+18}" y="23">{html.escape(name)}</text>')
    for tick in yticks:
        parts.append(f'<path d="M{left},{y(tick):.2f} H{width-right}" stroke="#e2e8f0" '
                     f'stroke-dasharray="{"" if tick == 0 else "3 5"}"/>'
                     f'<text x="{left-12}" y="{y(tick)+4:.2f}" text-anchor="end">{number(tick)}</text>')
    for tick in xticks:
        parts.append(f'<text x="{x(tick):.2f}" y="{height-bottom+25}" text-anchor="middle">{number(tick)}</text>')
    for index, (name, points) in enumerate(series):
        color = COLORS[index]
        if bars:
            edges, counts = bars
            for i, count in enumerate(counts[index]):
                slot = x(edges[i+1]) - x(edges[i])
                bw = slot * 0.86 / len(series)
                bx = x(edges[i]) + slot * 0.07 + index * bw
                parts.append(f'<rect x="{bx:.2f}" y="{y(count):.2f}" width="{bw:.2f}" '
                             f'height="{height-bottom-y(count):.2f}" rx="1.5" fill="{color}" fill-opacity="0.85">'
                             f'<title>{html.escape(name)} · {number(edges[i])}–{number(edges[i+1])}: {count} операций</title></rect>')
        else:
            coordinates = " ".join(f"{x(a):.2f},{y(b):.2f}" for a, b in points)
            parts.append(f'<polyline points="{coordinates}" fill="none" stroke="{color}" stroke-width="2.4" '
                         'stroke-linecap="round" stroke-linejoin="round"/>')
            if mode != "cdf":
                for a, b in points:
                    parts.append(f'<circle cx="{x(a):.2f}" cy="{y(b):.2f}" r="2.4" fill="{color}">'
                                 f'<title>{html.escape(name)} · {number(a)}: {number(b)}</title></circle>')
    parts.append(f'<text x="{(left+width-right)/2}" y="{height-10}" text-anchor="middle">{html.escape(xlabel)}</text></svg>')
    return "".join(parts)


def charts(cases, names):
    data = prepare(cases, names)
    unit = data["unit"]
    edges, counts = data["edges"], data["counts"]
    bars = [(name, [((edges[i] + edges[i+1]) / 2, count) for i, count in enumerate(row)])
            for name, row in zip(names, counts)]
    definitions = [
        ("trials", "По испытаниям", "Стабильность выполнения",
         plot(data["trials"], "Стабильность выполнения", "Номер испытания",
              f"Средняя длительность операции, {unit}", mode="trial")),
        ("percentiles", "Перцентили", "Распределение по операциям",
         plot(data["distributions"], "Распределение по операциям", f"Длительность операции, {unit}",
              "Операций завершилось, %", mode="cdf")),
        ("histogram", "Гистограмма", "Частота длительности операций",
         plot(bars, "Частота длительности операций", f"Длительность операции, {unit}",
              "Количество операций", bars=(edges, counts))),
        ("time", "Во времени", "Ход замеров",
         plot(data["times"], "Ход замеров", f"Время с начала замеров, {data['time_unit']}",
              f"Средняя длительность операции, {unit}")),
    ]
    return ('<div class="chart-group detail-plots">'
            + ''.join(f'<figure data-chart-panel="{key}"><figcaption>{title}</figcaption>{svg}</figure>'
                      for key, _, title, svg in definitions) + '</div>')
