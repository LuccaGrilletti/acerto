# Acerto

A small web app to manage orders and payments for a family home-cooked meal ("marmita") business.

Customers pick up meals throughout the month and settle everything at once, so the balance owed per customer has to be tracked reliably. Today this is done with notes and WhatsApp messages. Acerto replaces that with a proper record of orders, payments and outstanding balances.

> **Status:** early development. The project skeleton and the first entity are in place; features are being built incrementally.

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

`Client`, `User`, `WeeklyMenu`, `MenuItem`, `Order`, `OrderItem`, `Payment`

Design decisions:

- **The balance is never stored.** It is derived: sum of orders minus sum of payments. This avoids the two values drifting apart.
- **Payments are decoupled from orders.** A payment reduces the client's overall balance instead of settling a specific order, which matches how payments actually happen. "Pay on the spot" is simply a payment created together with the order.
- **Prices are snapshotted** in each order item, so changing the menu never rewrites history.
- **Orders and payments are not edited or deleted.** Mistakes are fixed with correcting entries, keeping an auditable trail.
- **Clients with history are deactivated, not removed.**

## Architecture

- Packages are organized by feature (`client`, `menu`, `order`, `payment`, ...), not by technical layer.
- Pragmatic hexagonal approach: the JPA entity doubles as the domain model and carries its own invariants, and ports are introduced only at real boundaries (for example, the payment gateway).
- Business rules live in services and entities, never in controllers, so a REST layer can be added later without rework.
- Schema is owned by Flyway; Hibernate runs with `ddl-auto: validate`.

## Current state

- [x] Project bootstrap (Spring Boot, Maven, Flyway, Docker Compose with Postgres)
- [x] `clients` migration
- [x] `Client` entity with domain invariants
- [ ] Unit tests for `Client`
- [ ] Client repository, service and CRUD screens
- [ ] Domain model: menu, orders, payments
- [ ] Balance calculation
- [ ] Authentication and authorization (ADMIN / CLIENT roles)
- [ ] Admin screens: menu, clients with balance, orders, payments
- [ ] Customer screens: order history and open balance
- [ ] PIX payments via API (QR code and webhook, with signature verification and idempotency)
- [ ] Deployment (planned: AWS Lightsail with Docker Compose, daily `pg_dump` backups to S3)

## Running locally

Requirements: JDK 25, Docker.

```bash
docker compose up -d
./mvnw spring-boot:run
```

The compose file exposes Postgres on host port **5433** (to avoid clashing with a local Postgres on 5432). Flyway applies the migrations on startup.

## Data and privacy

Real customer data is never committed to this repository. Tests and examples use fictional data only.

## License

[MIT](LICENSE)
