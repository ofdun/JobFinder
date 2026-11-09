DATASOURCE = {'type': 'grafana-postgresql-datasource', 'uid': 'measurements'}


def panel(number, title, sql, kind='timeseries', unit='short', width=12):
    names = {'mean': 'Средняя', 'peak': 'Пиковая', 'cpu_ms': 'CPU, мс',
             'cpu_percent': 'CPU, %', 'ram_mean_mb': 'RAM средняя, МБ',
             'ram_peak_mb': 'RAM пик, МБ', 'sampled_seconds': 'Измерено, с',
             'component': 'Компонент', 'requests_per_second': 'Запросов/с',
             'successful_rps': 'Успешные запросы/с', 'unexpected_rate': 'Ошибки',
             'ram_mb': 'RAM', 'users': 'Пользователи', 'latency_ms': 'Задержка',
             'bytes': 'Объём', 'error_rate': 'Ошибки', 'operation': 'Операция'}
    return {'id': number, 'title': title, 'type': kind,
            'gridPos': {'x': ((number - 1) % 2) * 12 if width == 12 else 0,
                        'y': ((number - 1) // 2) * 9, 'w': width, 'h': 9},
            'datasource': DATASOURCE,
            'targets': [{'refId': 'A', 'datasource': DATASOURCE, 'rawSql': sql,
                         'editorMode': 'code', 'rawQuery': True,
                         'format': 'time_series' if kind == 'timeseries' else 'table'}],
            'fieldConfig': {'defaults': {'unit': {'megabytes': 'suffix:МБ', 'ms': 'suffix:мс'}.get(unit, unit), 'decimals': 2,
                'color': {'mode': 'palette-classic'}, 'custom': {'axisCenteredZero': False}},
                'overrides': [{'matcher': {'id': 'byName', 'options': name},
                               'properties': [{'id': 'displayName', 'value': value}]}
                              for name, value in names.items()]},
            'options': {'legend': {'displayMode': 'list', 'placement': 'bottom'},
                        'tooltip': {'mode': 'multi'}, 'orientation': 'vertical',
                        'showValue': 'auto', 'groupWidth': 0.7, 'barWidth': 0.85}}


def query_variable(name, label, query):
    return {'name': name, 'label': label, 'type': 'query', 'datasource': DATASOURCE,
            'query': query, 'refresh': 1, 'sort': 0, 'multi': False, 'includeAll': False}


def dashboard(uid, title, panels, variables=None):
    return {'uid': uid, 'title': title, 'schemaVersion': 39, 'version': 1,
            'editable': False, 'timezone': 'utc', 'refresh': '',
            'time': {'from': 'now-24h', 'to': 'now'}, 'panels': panels,
            'templating': {'list': variables or []}, 'annotations': {'list': []}}


def definitions():
    grouped = "FROM resources_summary s JOIN runs r ON r.id=s.run_id WHERE s.component='backend' GROUP BY r.profile, r.mode ORDER BY r.profile, r.mode"
    comparison = [
        panel(1, 'Процессорное время приложения', "SELECT r.mode AS configuration, avg(s.cpu_seconds) * 1000 AS cpu_ms " + grouped, 'barchart', 'ms'),
        panel(2, 'Память приложения', "SELECT r.mode AS configuration, avg(s.ram_mean_mib) * 1.048576 AS mean, max(s.ram_peak_mib) * 1.048576 AS peak " + grouped, 'barchart', 'megabytes'),
        panel(3, 'Пропускная способность', 'SELECT mode AS configuration, avg(rps) AS requests_per_second FROM runs GROUP BY profile,mode ORDER BY profile,mode', 'barchart', 'reqps'),
        panel(4, 'Задержка p95', 'SELECT mode AS configuration, avg(p95_ms) AS p95 FROM runs GROUP BY profile,mode ORDER BY profile,mode', 'barchart', 'ms'),
        panel(5, 'Процессорное время мониторинга и Collector', "SELECT r.mode || ' · ' || s.component AS configuration, avg(s.cpu_seconds) * 1000 AS cpu_ms FROM resources_summary s JOIN runs r ON r.id=s.run_id WHERE s.component IN ('monitor','collector') GROUP BY r.profile,r.mode,s.component ORDER BY r.profile,r.mode,s.component", 'barchart', 'ms'),
        panel(6, 'Память мониторинга и Collector', "SELECT r.mode || ' · ' || s.component AS configuration, avg(s.ram_mean_mib) * 1.048576 AS mean, max(s.ram_peak_mib) * 1.048576 AS peak FROM resources_summary s JOIN runs r ON r.id=s.run_id WHERE s.component IN ('monitor','collector') GROUP BY r.profile,r.mode,s.component ORDER BY r.profile,r.mode,s.component", 'barchart', 'megabytes'),
        panel(7, 'Объём логов', 'SELECT mode AS configuration, avg(log_bytes) / 1000000.0 AS bytes FROM runs GROUP BY profile,mode ORDER BY profile,mode', 'barchart', 'megabytes'),
        panel(8, 'Ошибки запросов', 'SELECT mode AS configuration, sum(unexpected_errors)::double precision / NULLIF(sum(requests),0) AS error_rate FROM runs GROUP BY profile,mode ORDER BY profile,mode', 'barchart', 'percentunit')]
    selected = 'WHERE run_id=${run:sqlstring}'
    load = [
        panel(1, 'Задержка запросов', f'SELECT at AS time,p50_ms AS p50,p90_ms AS p90,p95_ms AS p95,p99_ms AS p99 FROM windows {selected} ORDER BY at', unit='ms'),
        panel(2, 'Запросы в секунду', f'SELECT at AS time,rps,successful_rps FROM windows {selected} ORDER BY at', unit='reqps'),
        panel(3, 'Виртуальные пользователи', f'SELECT at AS time,value AS users FROM vus {selected} ORDER BY at'),
        panel(4, 'Неожиданные ошибки', f'SELECT at AS time,unexpected_rate FROM windows {selected} ORDER BY at', unit='percentunit'),
        panel(5, 'Распределение задержки', f'SELECT latency_ms FROM requests {selected} AND NOT expected_error', 'histogram', 'ms'),
        panel(6, 'Задержки по операциям', f'SELECT operation,percentile_cont(0.5) WITHIN GROUP(ORDER BY latency_ms) AS p50,percentile_cont(0.95) WITHIN GROUP(ORDER BY latency_ms) AS p95,percentile_cont(0.99) WITHIN GROUP(ORDER BY latency_ms) AS p99 FROM requests {selected} AND NOT expected_error GROUP BY operation ORDER BY operation', 'barchart', 'ms')]
    resources = [
        panel(1, 'Загрузка CPU', f'SELECT at AS time,cpu_percent FROM resources {selected} AND component=${{component:sqlstring}} ORDER BY at', unit='percent'),
        panel(2, 'Рабочая память', f'SELECT at AS time,ram_mib * 1.048576 AS ram_mb FROM resources {selected} AND component=${{component:sqlstring}} ORDER BY at', unit='megabytes'),
        panel(3, 'Ресурсы компонентов', f'SELECT component,cpu_seconds * 1000 AS cpu_ms,cpu_percent,ram_mean_mib * 1.048576 AS ram_mean_mb,ram_peak_mib * 1.048576 AS ram_peak_mb,sampled_seconds FROM resources_summary {selected} ORDER BY component', 'table', width=24)]
    run = query_variable('run', 'Прогон', 'SELECT id FROM runs ORDER BY id')
    component = query_variable('component', 'Компонент', 'SELECT DISTINCT component FROM resources ORDER BY component')
    suite = query_variable('suite', 'Серия', 'SELECT DISTINCT suite FROM runs ORDER BY suite DESC')
    profile = query_variable('profile', 'Сценарий', 'SELECT DISTINCT profile FROM runs WHERE suite=${suite:sqlstring} ORDER BY profile')
    run['query'] = 'SELECT id FROM runs WHERE suite=${suite:sqlstring} ORDER BY id'
    for item in comparison:
        item['options']['orientation'] = 'horizontal'
        for target in item['targets']:
            target['rawSql'] = target['rawSql'].replace(
                'WHERE s.component', 'WHERE r.suite=${suite:sqlstring} AND r.profile=${profile:sqlstring} AND s.component').replace(
                'FROM runs GROUP', 'FROM runs WHERE suite=${suite:sqlstring} AND profile=${profile:sqlstring} GROUP')
    return [dashboard('comparison', 'Сравнение конфигураций', comparison, [suite, profile]),
            dashboard('load', 'Нагрузка', load, [suite, run]),
            dashboard('resources', 'Ресурсы', resources, [suite, run, component])]
