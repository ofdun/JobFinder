PROFILES = ('ramp', 'stress', 'soak', 'soak-10', 'soak-20')


def make_plan(profile, smoke=False, stable_vus=4, peak_vus=128, soak_seconds=1800, recovery_seconds=600):
    if profile not in PROFILES:
        raise ValueError('Unknown profile')
    if not 1 <= stable_vus < peak_vus <= 1024:
        raise ValueError('Require 1 <= stable VUs < peak VUs <= 1024')
    if not smoke and soak_seconds < 600:
        raise ValueError('A full soak must last at least 600 seconds')
    if not smoke and recovery_seconds < 30:
        raise ValueError('Recovery observation must include at least three ten-second windows')
    phases = []

    def add(name, seconds, vus, kind='measurement', plateau=True):
        phases.append(dict(name=name, seconds=seconds, vus=vus, kind=kind, plateau=plateau))

    add('warmup', 5 if smoke else 60, stable_vus, 'warmup')
    if profile == 'ramp':
        levels = [stable_vus, peak_vus] if smoke else sorted({stable_vus, 8, 16, 32, 64, peak_vus})
        levels = [n for n in levels if stable_vus <= n <= peak_vus]
        for index, vus in enumerate(levels):
            if index:
                add(f'ramp-{vus}', 3 if smoke else 15, vus, plateau=False)
            add(f'hold-{vus}', 8 if smoke else 45, vus)
    elif profile == 'stress':
        add('baseline', 8 if smoke else 60, stable_vus)
        add('overload-ramp', 3 if smoke else 15, peak_vus, plateau=False)
        add('overload', 8 if smoke else 120, peak_vus)
        add('return-to-normal', 3 if smoke else 15, stable_vus, plateau=False)
        add('recovery', 12 if smoke else recovery_seconds, stable_vus)
    else:
        add('steady', 15 if smoke else soak_seconds, stable_vus)
    return dict(profile=profile, smoke=smoke, phases=phases,
                error_percent={'soak-10': 10, 'soak-20': 20}.get(profile, 0),
                think_seconds=0.3 if smoke else 1.5, request_timeout_seconds=3,
                latency_limit_ms=500, error_limit=0.01, window_seconds=2 if smoke else 10,
                minimum_window_requests=5 if smoke else 20,
                minimum_samples=20 if smoke else 1000,
                recovery_windows=3, duration_seconds=sum(p['seconds'] for p in phases))


def cpu_layout(application='0,1', generator='2,3', monitor='4'):
    groups = {}
    for name, text in [('application', application), ('generator', generator), ('monitor', monitor)]:
        try:
            values = {int(part) for part in text.split(',')}
        except ValueError as error:
            raise ValueError('CPU sets must be comma-separated nonnegative integers') from error
        if not values or min(values) < 0:
            raise ValueError('Invalid CPU set')
        groups[name] = values
    if any(groups[a] & groups[b] for a, b in [('application', 'generator'), ('application', 'monitor'), ('generator', 'monitor')]):
        raise ValueError('Application, generator and monitoring CPUs must not overlap')
    return {name: ','.join(map(str, sorted(values))) for name, values in groups.items()}
