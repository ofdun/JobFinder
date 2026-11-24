import argparse
import json
import re
import time
from pathlib import Path
from urllib.parse import urlencode, urlparse
from urllib.request import Request, urlopen


def trace_files(source):
    if source.is_file():
        return [source]
    return sorted(source.rglob('traces.jsonl'))


def selected_payload(payload, selected):
    resources = []
    for resource in payload.get('resourceSpans', []):
        scopes = []
        for scope in resource.get('scopeSpans', []):
            spans = [span for span in scope.get('spans', [])
                     if selected is None or span['traceId'] in selected]
            if spans:
                scopes.append({**scope, 'spans': spans})
        if scopes:
            resources.append({**resource, 'scopeSpans': scopes})
    return {'resourceSpans': resources}


def import_traces(source, endpoint='http://localhost:4318/v1/traces', all_traces=False, trace_id=None):
    if urlparse(endpoint).hostname not in ('localhost', '127.0.0.1', '::1'):
        raise ValueError('Импорт разрешён только в локальный Collector')
    if trace_id is not None and not re.fullmatch(r'[a-f0-9]{32}', trace_id):
        raise ValueError('Trace ID должен содержать 32 шестнадцатеричных символа')
    files = trace_files(Path(source))
    if not files:
        raise ValueError('Файлы traces.jsonl не найдены')
    total = 0
    for path in files:
        preview = path.parent / 'traces/index.html'
        selected = ({trace_id} if trace_id else None if all_traces else set(re.findall(
            r'<h2>([a-f0-9]{32})</h2>', preview.read_text())))
        if selected == set():
            raise ValueError(f'В {preview} нет примеров трейсов')
        count = 0
        first = None
        start, stop = None, None
        with path.open() as stream:
            for line in stream:
                payload = selected_payload(json.loads(line), selected)
                if not payload['resourceSpans']:
                    continue
                body = json.dumps(payload).encode()
                request = Request(endpoint, data=body, headers={'Content-Type': 'application/json'})
                with urlopen(request, timeout=30) as response:
                    result = json.loads(response.read() or '{}')
                partial = result.get('partialSuccess', {})
                if int(partial.get('rejectedSpans', 0)) or partial.get('errorMessage'):
                    raise RuntimeError(f'Collector отклонил данные: {partial}')
                for resource in payload['resourceSpans']:
                    for scope in resource['scopeSpans']:
                        for span in scope['spans']:
                            count += 1
                            first = first or span['traceId']
                            began, ended = int(span['startTimeUnixNano']), int(span['endTimeUnixNano'])
                            start = min(start, began) if start is not None else began
                            stop = max(stop, ended) if stop is not None else ended
                time.sleep(len(body) / 2000000)
        total += count
        print(f'{path.parent.name}: отправлено {count} спанов', flush=True)
        if first:
            query = urlencode({'var-traceId': first, 'from': start // 1000000 - 1000,
                               'to': stop // 1000000 + 1000})
            print('http://localhost:3000/d/traces/trejsy?' + query, flush=True)
    if not total:
        raise ValueError('Подходящие спаны не найдены')
    return total


def main():
    parser = argparse.ArgumentParser(description='Импорт сохранённых трейсов CI в локальный Tempo')
    parser.add_argument('source', type=Path)
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument('--all', action='store_true', help='Весь экспорт вместо примеров из HTML')
    mode.add_argument('--trace-id', help='Загрузить один трейс по ID')
    args = parser.parse_args()
    import_traces(args.source, all_traces=args.all, trace_id=args.trace_id)


if __name__ == '__main__':
    main()
