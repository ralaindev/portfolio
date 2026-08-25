# FASE 1: Order Service

## Qué se ha construido

`order-service` permite crear y consultar pedidos. El pedido comienza en estado
`PENDING`, calcula su total dentro del dominio y se guarda junto con sus líneas
en PostgreSQL. Todavía no publica eventos: eso se añadirá con Transactional
Outbox en una fase posterior.

## Conceptos para estudiar

- **Aggregate root:** `Order` controla sus líneas y el cálculo del total; el
  exterior no modifica una línea aislada.
- **Value Object:** `Money`, `Quantity`, `OrderId` y otros tipos pequeños
  validan sus propias reglas y no dependen de Spring.
- **Syntactic validation:** las anotaciones de `CreateOrderRequest` validan la
  forma del JSON, por ejemplo `@Positive` para `quantity`.
- **Business validation:** `Order` rechaza un pedido sin líneas y `Money` rechaza
  importes negativos; estas reglas no dependen del controller.
- **Idempotency:** la aplicación reutiliza el pedido para la misma clave y
  contenido, mientras PostgreSQL respalda la garantía con `UNIQUE`.

## Archivos para revisar

- `domain/model/Order.java`
- `domain/model/Money.java`
- `application/usecase/CreateOrderService.java`
- `infrastructure/persistence/JpaOrderRepositoryAdapter.java`
- `infrastructure/web/OrderController.java`
- `src/main/resources/db/migration/V4__add_idempotency_key.sql`

## Pruebas manuales

```powershell
docker compose up -d postgres
.\mvnw.cmd -pl services/order-service spring-boot:run
```

En otra terminal:

```powershell
$body = '{"customerId":"ad2f3742-a4a6-4b9c-aed1-45884cc7bd21","lines":[{"productId":"55db5caf-b0da-49ab-b346-b7581a91ee76","quantity":2,"unitPrice":19.99}]}'
$headers = @{'Idempotency-Key'='manual-order-1'; 'X-Correlation-Id'='manual-test'}
$created = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/orders -Headers $headers -ContentType 'application/json' -Body $body
$created
Invoke-RestMethod http://localhost:8080/api/v1/orders/$($created.orderId)
```

Repite el POST con la misma clave y cuerpo. Debe devolver el mismo `orderId`.
Después cambia `quantity` a `3`; debe devolver `409 Conflict`.

## What I learned

Separar el dominio de JPA hace visible qué reglas pertenecen al negocio y qué
decisiones son solo de almacenamiento. La idempotencia no se puede resolver
únicamente con una consulta previa porque dos peticiones pueden pasar esa
comprobación al mismo tiempo.

## Known limitations

- La recuperación de un pedido todavía no está autorizada por usuario.
- No existe todavía outbox ni publicación de `OrderCreated`.
- La resolución de carreras de idempotencia concurrentes se reforzará junto con
  las transacciones y el outbox.
