# 1. Client account as an append-only ledger

- Date: 2026-10-09
- Status: Accepted

## Context

Customers pick up meals throughout the month and settle everything at once, so
the system must track what each customer owes and every payment they make. The
history of charges and payments has to be auditable: for any customer, it must
be possible to explain how the current balance was reached.

The first version is small, built for a family business, and its main
deliverable is a working replacement for notes and WhatsApp messages. The
project also has a learning goal, and event sourcing (ES) was considered as a
way to practice a pattern relevant to financial backends. Work on this project
is irregular, so the part with the most uncertainty should not sit on the
critical path to a usable first version.

## Decision

We will model the client account as an append-only ledger of typed entries
(charge, payment, adjustment), stored in the project's PostgreSQL database with
a Flyway-managed schema.

- Entries are never updated or deleted. A mistake is corrected by appending an
  adjustment or a reversal.
- The rest of the system talks to the account through an interface (charge,
  receive payment, query balance and history) that does not expose how entries
  are stored.
- Creating an order appends a charge entry in the same transaction. "Pay on the
  spot" is a charge and a payment appended by the same operation.
- The balance is derived from the entries and never stored (see ADR-0004).
- Event sourcing with Axon Framework is deferred to an experiment on a separate
  branch, after the first version is in real use.
- Money representation and the exact entry schema are defined at implementation
  time.

## Alternatives considered

1. **Event-sourced account with Axon Framework 5.** It would allow replaying
   arbitrary state and building new projections from past events. It is
   deferred, not rejected. The costs that led to deferring it: stored events
   must stay readable forever, so versioning has to be planned from the first
   event; reads go through projections; the JPA and Flyway stack revolves around
   current state, so the account would need its own read model and persistence
   code; the event schema is dictated by the framework and maintained through
   our own migrations; and the hardest part of the system would sit on the path
   to the first usable version.

   If the experiment goes ahead, it is bounded by these constraints: Axon
   Framework 5.x open-source modules only (Apache 2.0), the JPA event storage
   engine, no Axon Server, and none of the commercial modules (for example the
   PostgreSQL event store extension, which requires a paid license in
   production). Axon Framework 4 is end of life, so only 5.x is considered. The
   effort is bounded by logged working hours rather than dates, and a new ADR
   defines the budget and exit criteria when the experiment starts.
2. **Plain CRUD with a derived balance.** Rejected: immutability would depend on
   convention in each table, and there would be no single ordered record of
   changes to audit.

## Consequences

Positive:

- Full, auditable history of charges and payments. The balance at any past date
  can be computed by summing entries up to that date.
- Fits the existing stack (JPA, Flyway) and keeps entries and balance
  consistent in the same transaction, so screens never show a stale balance.
- Structurally close to an event stream, so the later ES experiment can be
  compared against it using real data.

Negative:

- State other than the balance cannot be reconstructed from the ledger, and new
  read models cannot be built from history beyond what the entries carry.
- The balance is computed on read. This is cheap at the expected scale, but may
  need a materialized value if it ever becomes slow.
- The learning goal around ES is postponed.