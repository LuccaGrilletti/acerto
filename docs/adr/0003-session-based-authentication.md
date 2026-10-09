# 3. Session-based authentication with Spring Security

- Date: 2026-10-09
- Status: Accepted

## Context

The application is a single deployable monolith that renders its pages on the
server with Thymeleaf. It has one kind of client, the browser, and two roles:
`ADMIN` (the family running the business) and `CLIENT` (customers viewing their
own orders and balance). It also handles payment data, so authentication and
request forgery protection matter.

## Decision

We will use Spring Security with stateful, session-based authentication and form
login.

- Credentials are stored as password hashes using Spring Security's password
  encoder; plain passwords are never stored or logged.
- Authorization is by role, enforced on URL groups and on service methods.
- CSRF protection stays enabled for all form submissions. Thymeleaf forms
  receive the token automatically.
- Session cookies are `HttpOnly`, `Secure` and `SameSite`, and HTTPS is
  mandatory in production.
- Endpoints that receive external notifications, such as the payment webhook,
  are not session-authenticated. They are exempt from CSRF only for that path
  and are authenticated by verifying the sender's signature, with idempotent
  processing.
- Secrets and credentials for external services come from environment
  variables or a secrets store, never from the repository.

## Alternatives considered

1. **Stateless JWT.** It solves the problem of several decoupled clients
   (mobile apps, a single-page app, third-party consumers) sharing an API
   without server-side state. This application has none of those. JWT would add
   token expiry and refresh handling, make revocation harder, and, if tokens
   are readable from JavaScript, increase the impact of XSS, without solving a
   problem the current design has.
2. **Delegating login to an external identity provider.** Rejected for the
   first version: it adds an external dependency and an account requirement for
   customers who currently use WhatsApp, with no benefit at this scale.

## Consequences

Positive:

- The simplest model that fits a server-rendered monolith, and the framework's
  default for form-based applications.
- Sessions can be invalidated on the server, for example on logout or when a
  user is deactivated.

Negative:

- Session state lives on the server. With a single instance this is fine, but
  running several instances would require sticky sessions or a shared session
  store.
- If the project later exposes a REST API to an external or decoupled client,
  token-based authentication may be needed for that API, and a new ADR would
  record the decision.