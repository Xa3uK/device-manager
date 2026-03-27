CREATE TABLE devices
(
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    brand      VARCHAR(255) NOT NULL,
    state      VARCHAR(50)  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL
);
