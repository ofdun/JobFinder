import json
from pathlib import Path
from argparse import ArgumentParser, Namespace
from html import escape

import lizard
from pygments.lexers import JavaLexer
from multimetric.cls.metric.operands import MetricBaseOperands
from multimetric.cls.metric.operators import MetricBaseOperator
from multimetric.cls.calc.halstead import MetricBaseCalcHalstead

ROOT = Path(__file__).resolve().parent.parent
LIMIT = 10
SOURCE_ROOTS = ("JobFinder/src", "JobFinder-CLI/src")


def sources(module=None):
    roots = (module / "src",) if module else tuple(ROOT / source for source in SOURCE_ROOTS)
    files = sorted(path for source in roots for path in source.rglob("*.java"))
    if not files:
        raise ValueError("Не найдены Java-исходники")
    return files


def cyclomatic(files):
    methods = []
    for path in files:
        analysis = lizard.analyze_file(str(path))
        for function in analysis.function_list:
            methods.append({
                "file": str(path.relative_to(ROOT)),
                "method": function.long_name,
                "line": function.start_line,
                "complexity": function.cyclomatic_complexity,
            })
    if not methods:
        raise ValueError("Анализатор не обнаружил методы")
    return methods


def halstead(files):
    results = {}
    args = Namespace(halstead_bug_predict_method="new")
    for path in files:
        tokens = list(JavaLexer().get_tokens(path.read_text(encoding="utf-8")))
        values = {}
        for metric_type in (MetricBaseOperands, MetricBaseOperator):
            metric = metric_type(args)
            metric.parse_tokens("Java", tokens)
            values.update(metric.get_results())
        values.pop("lang", None)
        results[str(path.relative_to(ROOT))] = MetricBaseCalcHalstead(args).get_results(values)
    return results


def render(report, output):
    rows = []
    for name, metrics in report["halstead"].items():
        complexity = max((m["complexity"] for m in report["methods"] if m["file"] == name), default=0)
        values = [escape(name), str(complexity)] + [
            f'{metrics[key]:.2f}' for key in ("halstead_volume", "halstead_difficulty", "halstead_effort")
        ]
        rows.append("<tr>" + "".join(f"<td>{value}</td>" for value in values) + "</tr>")
    output.write_text(
        '<!doctype html><html lang="ru"><meta charset="utf-8"><title>Статический анализ</title>'
        '<style>body{font:16px system-ui;margin:24px}table{border-collapse:collapse;width:100%}'
        'td,th{text-align:left;padding:8px;border-bottom:1px solid #ddd}td:first-child{overflow-wrap:anywhere}</style>'
        '<h1>Статический анализ</h1><p>Максимальная цикломатическая сложность метода: 10.</p>'
        '<p><a href="metrics.json">Метрики каждого метода и исходные счётчики Холстеда</a></p>'
        '<table><thead><tr><th>Файл</th><th>Максимум CC</th><th>Объём Холстеда</th>'
        '<th>Сложность Холстеда</th><th>Усилия Холстеда</th></tr></thead><tbody>'
        + "".join(rows) + '</tbody></table></html>'
    )


def main():
    parser = ArgumentParser()
    parser.add_argument("--module", type=Path)
    args = parser.parse_args()
    module = args.module.resolve() if args.module else None
    files = sources(module)
    methods = cyclomatic(files)
    measures = halstead(files)
    output = (module or ROOT / "JobFinder") / "build/reports/quality"
    output.mkdir(parents=True, exist_ok=True)
    report = {"limit": LIMIT, "methods": methods, "halstead": measures}
    (output / "metrics.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n")
    render(report, output / "index.html")
    violations = [method for method in methods if method["complexity"] > LIMIT]
    for item in violations:
        print(f'{item["file"]}:{item["line"]}: {item["method"]}: {item["complexity"]} > {LIMIT}')
    print(f'Файлов: {len(files)}; методов: {len(methods)}; максимум CC: {max(m["complexity"] for m in methods)}; превышений: {len(violations)}')
    print(f"Холстед: {output / 'metrics.json'}")
    return bool(violations)


if __name__ == "__main__":
    raise SystemExit(main())
