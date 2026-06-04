CREATE TABLE device_entity (
    id          SERIAL PRIMARY KEY,
    device_id   UUID            NOT NULL UNIQUE,
    name        VARCHAR(255)    NOT NULL,
    type        VARCHAR(100)    NOT NULL,
    ip_address  VARCHAR(50)     NOT NULL,
    location    VARCHAR(255)    NOT NULL,
    status      VARCHAR(20)     NOT NULL CHECK (status IN ('ONLINE', 'OFFLINE', 'DEGRADED')),
    is_stale    BOOLEAN         DEFAULT FALSE,
    created_at  TIMESTAMP       NOT NULL,
    last_report TIMESTAMP
);

CREATE TABLE report_entity (
    id            SERIAL PRIMARY KEY,
    device_id     UUID            NOT NULL,
    device_status VARCHAR(20)     NOT NULL CHECK (device_status IN ('ONLINE', 'OFFLINE', 'DEGRADED')),
    message       TEXT,
    created_at    TIMESTAMP       NOT NULL
);

CREATE TABLE report_outer_box (
    id            SERIAL PRIMARY KEY,
    device_id     UUID            NOT NULL,
    device_status VARCHAR(20)     NOT NULL CHECK (device_status IN ('ONLINE', 'OFFLINE', 'DEGRADED')),
    message       TEXT,
    created_at    TIMESTAMP       NOT NULL
);