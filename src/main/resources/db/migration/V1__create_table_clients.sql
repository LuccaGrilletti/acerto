CREATE TABLE clients (
    id      UUID PRIMARY KEY,
    nome    VARCHAR(120) NOT NULL,
    contato VARCHAR(40),
    ativo   BOOLEAN NOT NULL DEFAULT TRUE
);