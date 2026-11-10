CREATE TABLE runs (
    id TEXT PRIMARY KEY,
    suite TEXT NOT NULL,
    profile TEXT NOT NULL,
    mode TEXT NOT NULL,
    smoke BOOLEAN NOT NULL,
    started DOUBLE PRECISION NOT NULL,
    stopped DOUBLE PRECISION NOT NULL,
    requests BIGINT NOT NULL,
    unexpected_errors BIGINT NOT NULL,
    rps DOUBLE PRECISION,
    p95_ms DOUBLE PRECISION,
    log_bytes BIGINT NOT NULL
);
CREATE TABLE resources_summary (
    run_id TEXT REFERENCES runs ON DELETE CASCADE,
    component TEXT NOT NULL,
    cpu_seconds DOUBLE PRECISION,
    cpu_percent DOUBLE PRECISION,
    ram_mean_mib DOUBLE PRECISION,
    ram_peak_mib DOUBLE PRECISION,
    sampled_seconds DOUBLE PRECISION
);
CREATE TABLE resources (
    run_id TEXT REFERENCES runs ON DELETE CASCADE,
    component TEXT NOT NULL,
    at TIMESTAMPTZ NOT NULL,
    cpu_percent DOUBLE PRECISION,
    ram_mib DOUBLE PRECISION
);
CREATE TABLE windows (
    run_id TEXT REFERENCES runs ON DELETE CASCADE,
    at TIMESTAMPTZ NOT NULL,
    rps DOUBLE PRECISION,
    successful_rps DOUBLE PRECISION,
    unexpected_rate DOUBLE PRECISION,
    p50_ms DOUBLE PRECISION,
    p90_ms DOUBLE PRECISION,
    p95_ms DOUBLE PRECISION,
    p99_ms DOUBLE PRECISION
);
CREATE TABLE vus (
    run_id TEXT REFERENCES runs ON DELETE CASCADE,
    at TIMESTAMPTZ NOT NULL,
    value DOUBLE PRECISION
);
CREATE TABLE requests (
    run_id TEXT REFERENCES runs ON DELETE CASCADE,
    at TIMESTAMPTZ NOT NULL,
    operation TEXT NOT NULL,
    latency_ms DOUBLE PRECISION NOT NULL,
    expected_error BOOLEAN NOT NULL
);
CREATE INDEX ON resources(run_id,component,at);
CREATE INDEX ON windows(run_id,at);
CREATE INDEX ON vus(run_id,at);
CREATE INDEX ON requests(run_id);
CREATE USER grafana WITH PASSWORD 'grafana';
GRANT CONNECT ON DATABASE metrics TO grafana;
GRANT USAGE ON SCHEMA public TO grafana;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO grafana;
ALTER USER grafana SET default_transaction_read_only=on;
