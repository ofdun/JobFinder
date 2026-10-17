import concurrent.futures
import http.client
import json
import socket
import time
from pathlib import Path


class DockerConnection(http.client.HTTPConnection):
    def __init__(self):
        super().__init__('localhost', timeout=5)

    def connect(self):
        self.sock = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
        self.sock.settimeout(self.timeout)
        self.sock.connect('/var/run/docker.sock')


def read_stats(target):
    client = DockerConnection()
    try:
        client.request('GET', f'/containers/{target["id"]}/stats?stream=false&one-shot=true')
        response = client.getresponse()
        if response.status != 200:
            raise RuntimeError(f'Docker stats: {response.status}')
        return target['name'], json.loads(response.read())
    finally:
        client.close()


def counters(data):
    memory = data.get('memory_stats', {})
    cache = memory.get('stats', {}).get('inactive_file', memory.get('stats', {}).get('total_inactive_file', 0))
    block = data.get('blkio_stats', {})
    io = (block['io_service_bytes_recursive'] or []) if 'io_service_bytes_recursive' in block else None
    return {
        'cpu_ns': data.get('cpu_stats', {}).get('cpu_usage', {}).get('total_usage', 0),
        'ram_bytes': max(0, memory.get('usage', 0) - cache),
        'disk_read_bytes': None if io is None else sum(x['value'] for x in io if x['op'].lower() == 'read'),
        'disk_write_bytes': None if io is None else sum(x['value'] for x in io if x['op'].lower() == 'write'),
    }


def main():
    root = Path('/results')
    targets = json.loads((root / 'targets.json').read_text())
    previous = {}
    with (root / 'resources.jsonl').open('w') as output, concurrent.futures.ThreadPoolExecutor(max_workers=len(targets)) as executor:
        while not (root / 'stop-monitor').exists():
            started = time.monotonic()
            futures = {executor.submit(read_stats, target): target for target in targets}
            for future in concurrent.futures.as_completed(futures):
                target = futures[future]
                now = time.time()
                try:
                    name, data = future.result()
                    values = counters(data)
                    before = previous.get(name)
                    record = {'time': now, 'component': name, **values}
                    if before:
                        elapsed = now - before['time']
                        record['cpu_percent'] = max(0, values['cpu_ns'] - before['cpu_ns']) / elapsed / 1e7
                        for key in ('disk_read_bytes', 'disk_write_bytes'):
                            record[key + '_per_second'] = (max(0, values[key] - before[key]) / elapsed
                                                          if values[key] is not None and before[key] is not None else None)
                    previous[name] = {'time': now, **values}
                except Exception as error:
                    record = {'time': now, 'component': target['name'], 'error': str(error)}
                output.write(json.dumps(record) + '\n')
            output.flush()
            time.sleep(max(0, 1 - (time.monotonic() - started)))


if __name__ == '__main__':
    main()
