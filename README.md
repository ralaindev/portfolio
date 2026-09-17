# Decskill Price API

REST API technical test for consulting and managing product prices by brand and
application date.

## Current Scope

The current test suite covers:

- Looking up the applicable price for a product and brand at a given instant.
- Creating, reading, updating, and deleting prices.
- Selecting the applicable price according to its validity interval and
  priority.
- ISO-8601 date-time values with different offsets.
- Validation errors, missing parameters, malformed request bodies, unknown
  resources, and missing applicable prices.

## API

### Find an applicable price

```text
GET /api/v1/prices?queryDate={queryDate}&productId={productId}&brandId={brandId}
```

All query parameters are required. `queryDate` must be an ISO-8601 date-time;
`productId` and `brandId` must be positive numbers.

Example:

```bash
curl "http://localhost:8080/api/v1/prices?queryDate=2020-06-14T16:00:00%2B02:00&productId=35455&brandId=1"
```

Successful responses contain:

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 2,
  "startDate": "2020-06-14T15:00:00+02:00",
  "endDate": "2020-06-14T18:30:00+02:00",
  "price": 25.45,
  "currency": "EUR"
}
```

### Manage prices

| Method | Endpoint | Success response |
|---|---|---|
| `POST` | `/api/v1/prices` | `201 Created` with a `Location` header |
| `GET` | `/api/v1/prices/{id}` | `200 OK` |
| `PUT` | `/api/v1/prices/{id}` | `200 OK` |
| `DELETE` | `/api/v1/prices/{id}` | `204 No Content` |

Create and update requests use this JSON shape:

```json
{
  "brandId": 1,
  "productId": 99999,
  "priceList": 90001,
  "priority": 2,
  "startDate": "2020-06-14T15:00:00+02:00",
  "endDate": "2020-06-14T18:30:00+02:00",
  "price": 25.45,
  "currency": "EUR"
}
```

Dates must describe valid instants, `currency` is a three-letter uppercase
code, and numeric values must satisfy the API validation rules. A zero price
is valid.

Errors are returned as JSON with an HTTP `status` and a descriptive `message`.
Invalid input returns `400 Bad Request`; an unknown price or an interval with
no applicable price returns `404 Not Found`.

## Technology

- Java 21
- Spring Boot 4
- Spring MVC and MockMvc
- Spring Data JPA and Hibernate
- H2 for the test database
- Flyway for database migrations
- JUnit 5

## Tests

The integration tests are located at:

```text
src/test/java/com/decskill/test/infrastructure/input/rest/
```

Run the test suite from the Maven project root:

```bash
mvn test
```

The current commit contains the integration-test sources and generated reports;
the Maven build file and application sources must be present in the project
root for this command to run.

The latest generated test reports contain 36 tests with no failures: 13 API
lookup tests and 23 CRUD tests.
