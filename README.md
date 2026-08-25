# Distributed Orders Platform

Distributed order processing example built incrementally to study Java and
Spring backend concepts.

The repository starts with a small bootstrap and will grow one phase at a time.
The first phase intentionally contains only the `order-service` shell, its
PostgreSQL database, Flyway, and Actuator. Business behaviour is added in later
phases so each decision can be tested and explained.

## Current Phase: 2

The first order API is now available:

- `POST /api/v1/orders` creates a `PENDING` order.
- `GET /api/v1/orders/{orderId}` retrieves an order.
- `Idempotency-Key` is required when creating an order.
- Invalid request syntax and business conflicts use `ProblemDetail`.
- OpenAPI is available at `/swagger-ui.html` and `/v3/api-docs`.
- The importable contract is versioned at
  `contracts/order-service-openapi.yaml`.
- `inventory-service` provides the same Swagger setup on port `8081` with its
  contract at `contracts/inventory-service-openapi.yaml`.

## Phase 0, Phase 1, and Phase 2

Implemented:

- Maven multi-module project using Java 21 and Spring Boot 3.
- Runnable `order-service` module with an order creation API.
- PostgreSQL database named `orders_db` for local development.
- Flyway baseline migration and Actuator health endpoint.
- Domain, MVC, and PostgreSQL Testcontainers tests.
- `inventory-service` with optimistic and optional pessimistic stock locking.
- JPA query lab covering N+1, fetch join, `@EntityGraph`, and DTO projection.
- Minimal GitHub Actions verification workflow.

Not implemented yet: Kafka, RabbitMQ, security rules, distributed transactions,
outbox publishing, and the remaining services beyond orders and inventory.

## Quick Start

Requirements: JDK 21, Docker Desktop with Compose, and internet access for the
first Maven Wrapper run.

```powershell
docker compose up -d postgres
.\mvnw.cmd clean verify
.\mvnw.cmd -pl services/order-service spring-boot:run
```

These commands are intended for Windows PowerShell.

The service exposes Actuator at `http://localhost:8080/actuator/health`.

Create an order:

```bash
curl -i -X POST http://localhost:8080/api/v1/orders \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: demo-order-1' \
  -d '{"customerId":"ad2f3742-a4a6-4b9c-aed1-45884cc7bd21","lines":[{"productId":"55db5caf-b0da-49ab-b346-b7581a91ee76","quantity":2,"unitPrice":19.99}]}'
```

Use the returned `orderId` with:

```bash
curl http://localhost:8080/api/v1/orders/{orderId}
```

## Repository Layout

```text
services/order-service/    First service, built with hexagonal boundaries later
infrastructure/docker/     Local infrastructure notes and future service assets
docs/adr/                  Short architecture decisions
docs/learning/             Spanish study notes
```

## What I Learned

- A multi-module build gives each service an independent build boundary while
  keeping local development simple.
- Flyway migrations are versioned application code and must not be edited after
  they have been applied.
- A domain model should not need Spring to enforce its own invariants.
- A database unique constraint is the final protection for an idempotency key;
  checking first in Java is not enough under concurrency.
- A transactional outbox keeps the order change and its integration event in
  the same database transaction; a later worker will publish pending rows.

## Known Limitations

- Only `order-service` and `inventory-service` exist so far; later phases add
  brokers and the remaining services.
- The create operation stores `OrderCreated` in an outbox but does not publish
  events to Kafka yet.
- The application currently has no authentication or business API.

## Roadmap

See the phase plan in the project brief and the implementation decisions in
`docs/adr/`.
