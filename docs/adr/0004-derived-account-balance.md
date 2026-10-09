# 4. Derived account balance, decoupled from orders

- Date: 2026-10-09
- Status: Accepted

## Context

Customers collect meals throughout the month and pay later, sometimes in a
single payment and sometimes partially. In practice a payment is made against
what the customer owes overall, not against one particular meal. The system
needs a balance per customer that can be trusted.

## Decision

The balance is never stored. It is derived from the client account ledger
(ADR-0001) as the sum of charges, minus payments, plus signed adjustments.

- A payment reduces the client's overall balance. It is not linked to a specific
  order and does not mark any order as paid.
- "Pay on the spot" requires no special state: it is a charge and a payment
  appended by the same operation.
- Order items snapshot the unit price at the time of the order, so changing the
  menu never rewrites past charges.
- Orders and ledger entries are immutable. Corrections are new entries
  (adjustment or reversal), never edits or deletes.
- Nothing may write a balance value directly. Any value shown for performance
  reasons is a cache that can be rebuilt from the ledger.

## Alternatives considered

1. **Store the balance as a column on the client.** Faster reads, but it
   duplicates information already in the ledger. A failure between inserting an
   entry and updating the balance leaves the two out of step with no way to
   know which one is right.
2. **Allocate each payment to specific orders.** More traceable per order, but
   it forces someone to decide which meal each payment covers, which does not
   match how payments happen, and adds allocation logic that exists only to
   satisfy the model.

## Consequences

Positive:

- A single source of truth, so the balance cannot drift from the entries.
- Matches real payment behavior, and keeps pay-on-the-spot trivial.
- The history that explains the balance is the same data the balance is computed
  from.

Negative:

- The balance is computed on read. At the expected volume this is cheap with an
  index on the client identifier. If it ever becomes slow, a rebuildable cache
  or materialized value can be added without changing the model.
- The system cannot say which individual order is unpaid, only how much the
  client owes overall. If that is needed later, it must be derived by a stated
  rule (for example, oldest charges settled first) and not stored as a fact.