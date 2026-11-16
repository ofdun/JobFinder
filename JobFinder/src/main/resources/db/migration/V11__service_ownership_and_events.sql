-- Existing business tables and IDs remain intact. New service-owned state is additive.
CREATE SCHEMA IF NOT EXISTS accounts;
CREATE SCHEMA IF NOT EXISTS matching;
CREATE TABLE accounts.rpc_requests (
    request_id varchar(240) PRIMARY KEY,
    request_hash bytea NOT NULL,
    response bytea NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);
CREATE SEQUENCE jobfinder.resume_event_version;
CREATE TABLE jobfinder.event_outbox (
    event_id varchar(36) PRIMARY KEY,
    aggregate_id bigint NOT NULL,
    version bigint NOT NULL UNIQUE,
    payload bytea NOT NULL,
    trace_parent text NOT NULL DEFAULT '',
    created_at timestamptz NOT NULL DEFAULT now(),
    available_at timestamptz NOT NULL DEFAULT now(),
    attempts integer NOT NULL DEFAULT 0,
    published_at timestamptz
);
CREATE INDEX event_outbox_pending ON jobfinder.event_outbox(available_at,version) WHERE published_at IS NULL;
CREATE TABLE matching.resume_index (
    resume_id bigint PRIMARY KEY,
    version bigint NOT NULL,
    deleted boolean NOT NULL DEFAULT false,
    event_id varchar(36) NOT NULL,
    updated_at timestamptz NOT NULL DEFAULT now()
);
