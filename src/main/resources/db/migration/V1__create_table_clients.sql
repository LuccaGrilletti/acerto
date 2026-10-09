CREATE TABLE clients (
    id              UUID PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    phone           VARCHAR(25) NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE
);