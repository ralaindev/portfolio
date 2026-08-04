# Distributed Orders Platform

Personal Java backend portfolio built incrementally to practise concepts that are
useful in mid-senior backend interviews.

The repository starts with a small bootstrap and will grow one phase at a time.
The first phase intentionally contains only the `order-service` shell, its
PostgreSQL database, Flyway, and Actuator. Business behaviour is added in later
phases so each decision can be tested and explained.

## Phase 0

Implemented:

- Maven multi-module project using Java 21 and Spring Boot 3.
- Empty but runnable `order-service` module.
- PostgreSQL database named `orders_db` for local development.
- Flyway baseline migration and Actuator health endpoint.
- Context smoke test.
- Minimal GitHub Actions verification workflow.

Not implemented yet: orders, Kafka, RabbitMQ, security rules, distributed
transactions, and observability infrastructure beyond basic Actuator.

## Quick Start

Requirements: JDK 21, Docker Desktop with Compose, and internet access for the
first Maven Wrapper run.

```bash
docker compose up -d postgres
bash ./mvnw clean verify
bash ./mvnw -pl services/order-service spring-boot:run
```

On Windows, use `mvnw.cmd` instead of `bash ./mvnw`.

The service exposes Actuator at `http://localhost:8080/actuator/health`.

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
- Health checks are useful even before a service has business endpoints.

## Known Limitations

- Only the `order-service` shell exists in Phase 0.
- The local Compose file starts PostgreSQL only; later phases add brokers and
  the remaining services.
- The application currently has no authentication or business API.

## Roadmap

See the phase plan in the project brief and the implementation decisions in
`docs/adr/`.
