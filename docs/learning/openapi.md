# OpenAPI In This Project

## What It Is

OpenAPI is a machine-readable description of an HTTP API. It describes paths,
headers, request bodies, responses, schemas, and examples.

Swagger is the name of the original specification and ecosystem. OpenAPI is
the current specification name. Swagger UI is a web interface that renders an
OpenAPI document and lets us try requests.

## Decision

This project uses a small Contract First boundary for the HTTP contract:

```text
contracts/order-service-openapi.yaml
              |
              v
Maven copies the contract to the application static resources
              |
              v
Swagger UI loads /openapi/order-service-openapi.yaml
              |
              v
OrderController implements the HTTP behavior manually
```

The source of truth is:

```text
contracts/order-service-openapi.yaml
```

`inventory-service` follows the same arrangement with:

```text
contracts/inventory-service-openapi.yaml
```

The controller no longer repeats operation descriptions with `@Operation` or
`@Parameter`. It still owns HTTP routing, validation annotations, and the
translation to application commands through `OrderWebMapper`.

## Code First And Contract First

Code First starts with Java and lets Springdoc inspect controllers and DTOs to
produce an OpenAPI document. It is convenient for a small API, but the
contract can become an accidental side effect of implementation details.

Contract First starts with a versioned OpenAPI document. The implementation is
then checked against an explicit public contract. This is useful here because
the YAML can be imported into Postman and reviewed independently from Java.

The project does not use OpenAPI Generator yet. Generating server interfaces
and transport models for two endpoints would add more code than value. The
manual `OrderWebMapper` keeps the transport boundary visible without coupling
the domain to OpenAPI classes.

## OpenAPI Generator Later

OpenAPI Generator may become useful when another service needs to consume an
HTTP API. The first candidate would be a generated client inside that
consumer's HTTP adapter. Server-side interface generation can be reconsidered
when the API has more endpoints and a stable contract.

Generated classes must remain outside `domain`, `application`, and persistence
packages. They must be mapped to commands and domain objects explicitly.

## Useful URLs

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/openapi/order-service-openapi.yaml
http://localhost:8080/v3/api-docs
http://localhost:8081/swagger-ui.html
http://localhost:8081/openapi/inventory-service-openapi.yaml
```

The first two URLs use the versioned YAML contract. The `/v3/api-docs`
endpoint is retained by Springdoc for diagnostics, but it is not the source of
truth for the public API documentation.
