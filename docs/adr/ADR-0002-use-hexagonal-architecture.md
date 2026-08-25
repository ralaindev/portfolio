# ADR-0002: Use hexagonal architecture

## Context

The order flow needs HTTP, JPA, and later messaging, but its business rules
should remain understandable without starting Spring.

## Decision

Keep the domain model free of framework dependencies. Application use cases
depend on input and output ports. Controllers and JPA adapters live in the
infrastructure layer and map their own DTOs or entities.

## Alternatives considered

- A traditional controller-service-repository package layout would be shorter,
  but it makes infrastructure dependencies easier to leak into the domain.
- Sharing JPA entities with the domain would reduce mapping code but couple
  business rules to persistence concerns.

## Consequences

- The project has explicit mapping code and a few more classes.
- Domain tests run without Spring or a database.
- The architecture is easier to extend with an outbox and message adapters.
