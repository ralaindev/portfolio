# ADR-0001: Use microservices

## Context

The business flow has separate responsibilities for orders, inventory,
payments, queries, and notifications.

## Decision

Use a small monorepo with one independently deployable module per service. Each
service owns its domain model and data. Phase 0 starts with only `order-service`.

## Alternatives considered

- A modular monolith would be simpler initially, but would hide service
  boundaries and message-driven failure modes that the project needs to study.
- Separate repositories would make early iteration and local changes harder.

## Consequences

- Service boundaries and integration failures can be demonstrated explicitly.
- Local infrastructure and testing are more complex.
- The project must avoid sharing JPA entities and domain logic between services.
