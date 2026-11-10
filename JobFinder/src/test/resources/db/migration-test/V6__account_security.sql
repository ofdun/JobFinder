CREATE TABLE jobfinder.account_security (
    id VARCHAR(100) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    payload TEXT NOT NULL
);
