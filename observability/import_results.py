import argparse
import csv
import json
import subprocess
import sys
import tempfile
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, str(ROOT.parent / 'load-testing'))
from report import read_samples
from observability import LABELS


def timestamp(value):
    return datetime.fromtimestamp(value, timezone.utc).isoformat()


def copy_rows(stream, name, rows):
    stream.write(f"COPY {name} FROM STDIN WITH (FORMAT csv, NULL '');\n")
    writer = csv.writer(stream, lineterminator='\n')
    writer.writerows(rows)
    stream.write('\\.\n')


def import_results(source, suite, project):
    comparison = json.loads((source / 'comparison.json').read_text())
    with tempfile.TemporaryFile(mode='w+') as sql:
        sql.write("BEGIN;\nDELETE FROM runs WHERE suite=:'suite';\n")
        for item in comparison['runs']:
            root = source / item['run']
            summary = json.loads((root / 'summary.json').read_text())
            identifier = suite + '/' + item['run']
            start, stop = summary['measurement_start'], summary['measurement_stop']
            copy_rows(sql, 'runs', [(identifier, suite, item['profile'], LABELS[item['mode']],
                item['smoke'], start, stop, item['requests'], item['unexpected_errors'],
                item['rps'], item['p95_ms'], item['logs']['bytes'])])
            copy_rows(sql, 'resources_summary', [(identifier, name,
                *[values[k] for k in ('cpu_seconds','cpu_percent','ram_mean_mib','ram_peak_mib','sampled_seconds')])
                for name, values in item['resources'].items()])
            with (root / 'resources.jsonl').open() as data:
                records = (json.loads(line) for line in data)
                copy_rows(sql, 'resources', ((identifier, r['component'], timestamp(r['time']),
                    r.get('cpu_percent'), r['ram_bytes'] / 2 ** 20)
                    for r in records if 'error' not in r and start <= r['time'] <= stop))
            copy_rows(sql, 'windows', [(identifier, timestamp(w['time']),
                *[w[k] for k in ('rps','successful_rps','unexpected_rate','p50_ms','p90_ms','p95_ms','p99_ms')])
                for w in summary['windows']])
            samples, _, vus = read_samples(root / 'samples.gz')
            copy_rows(sql, 'vus', [(identifier, timestamp(t), v) for t, v in vus if start <= t <= stop])
            copy_rows(sql, 'requests', [(identifier, timestamp(s['time']), s['operation'],
                s['latency_ms'], s.get('expected_error') == 'true')
                for s in samples if start <= s['time'] < stop])
        sql.write('COMMIT;\n')
        sql.seek(0)
        subprocess.run(['docker','compose','-p',project,'-f',str(ROOT / 'compose.yml'),
            'exec','-T','metrics-db','psql','-X','-v','ON_ERROR_STOP=1','-v','suite='+suite,
            '-U','metrics','-d','metrics'],stdin=sql,check=True)
    return comparison


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('source', type=Path)
    parser.add_argument('--suite')
    parser.add_argument('--project', default='project')
    args = parser.parse_args()
    import_results(args.source, args.suite or args.source.name, args.project)
