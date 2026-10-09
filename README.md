# Acerto

A small web app to manage orders and payments for a family home-cooked meal ("marmita") business.

Customers pick up meals throughout the month and settle everything at once, so the balance owed per customer has to be tracked reliably. Today this is done with notes and WhatsApp messages. Acerto replaces that with a proper record of orders, payments and outstanding balances.

> **Status:** early development. The project skeleton, the first entity and the core architecture decisions are in place; features are being built incrementally.

## Goals

- **Admin area:** edit the weekly menu, register orders, record payments, see who owes what.
- **Customer area:** see recent orders and open balances.
- **Focus on backend and business rules.** The UI is server-rendered and intentionally simple.

## Tech stack

| Area | Choice |
|---|---|
| Language / build | Java 25, Maven |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Validation, Security) |
| Views | Thymeleaf, session-based authentication |
| Database | PostgreSQL 17 (Docker Compose for local development) |
| Migrations | Flyway |
| Other | Lombok |

## Domain model (planned)

`Client`, `User`, `WeeklyMenu`, `MenuItem`, `Order`, `OrderItem`, and the client account ledger (charge, payment and adjustment entries).

Design decisions:

- **The balance is never stored.** It is derived from the client account ledger: charges minus payments, plus signed adjustments. This avoids the two values drifting apart ([ADR-0004](docs/adr/0004-derived-account-balance.md)).
- **Payments are decoupled from orders.** A payment reduces the client's overall balance instead of settling a specific order, which matches how payments actually happen. "Pay on the spot" is a charge and a payment appended together.
- **The account is an append-only ledger.** Entries are never edited or deleted; mistakes are fixed with adjustment or reversal entries, keeping an auditable trail. The rest of the system talks to it through an interface ([ADR-0001](docs/adr/0001-client-account-as-append-only-ledger.md)).
- **Prices are snapshotted** in each order item, so changing the menu never rewrites history.
- **Clients with history are deactivated, not removed.**

## Architecture

- Packages are organized by feature (`client`, `menu`, `order`, `account`, ...), not by technical layer.
- Pragmatic hexagonal approach: the JPA entity doubles as the domain model and carries its own invariants, and ports are introduced only at real boundaries, such as the client account and the payment gateway ([ADR-0002](docs/adr/0002-jpa-entity-as-domain-model.md)).
- Authentication is session-based with Spring Security, with CSRF protection on; external webhooks are authenticated by signature, not by session ([ADR-0003](docs/adr/0003-session-based-authentication.md)).
- Business rules live in services and entities, never in controllers, so a REST layer can be added later without rework.
- Schema is owned by Flyway; Hibernate runs with `ddl-auto: validate`.
- Planned experiment: an event-sourced implementation of the client account with Axon Framework (open-source modules only), on a separate branch and compared against the ledger once the first version is in use.

## Architecture decision records

Decisions and their trade-offs are recorded in [`docs/adr`](docs/adr):

| ADR | Decision |
|---|---|
| [0001](docs/adr/0001-client-account-as-append-only-ledger.md) | Client account as an append-only ledger (event sourcing deferred to an experiment) |
| [0002](docs/adr/0002-jpa-entity-as-domain-model.md) | JPA entity as the domain model |
| [0003](docs/adr/0003-session-based-authentication.md) | Session-based authentication with Spring Security |
| [0004](docs/adr/0004-derived-account-balance.md) | Derived account balance, decoupled from orders |

## Current state

- [x] Project bootstrap (Spring Boot, Maven, Flyway, Docker Compose with Postgres)
- [x] `clients` migration
- [x] `Client` entity with domain invariants
- [x] Architecture decision records (0001 to 0004)
- [ ] Unit tests for `Client`
- [ ] Client repository, service and CRUD screens
- [ ] Menu and orders (immutable once created)
- [ ] Client account ledger and balance calculation
- [ ] Authentication and authorization (ADMIN / CLIENT roles)
- [ ] Admin screens: menu, clients with balance, orders, payments
- [ ] Customer screens: order history and open balance
- [ ] PIX payments via API (QR code and webhook, with signature verification and idempotency)
- [ ] Deployment (planned: AWS Lightsail with Docker Compose, daily `pg_dump` backups to S3)
- [ ] Event sourcing experiment with Axon Framework (separate branch)

## Running locally

Requirements: JDK 25, Docker.

```bash
docker compose up -d
./mvnw spring-boot:run
```

On Windows PowerShell, use `.\mvnw.cmd spring-boot:run`.

The compose file exposes Postgres on host port **5433** (to avoid clashing with a local Postgres on 5432). Flyway applies the migrations on startup.

## Data and privacy

Real customer data is never committed to this repository. Tests and examples use fictional data only.

## License

[MIT](LICENSE)