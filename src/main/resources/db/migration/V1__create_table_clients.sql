CREATE TABLE clients (
    id              UUID PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    phone           VARCHAR(40),
    active          BOOLEAN NOT NULL DEFAULT TRUE
);