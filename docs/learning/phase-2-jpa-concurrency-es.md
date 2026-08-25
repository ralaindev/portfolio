# FASE 2: JPA y concurrencia

## Qué se ha construido

`inventory-service` mantiene stock y permite reservar una unidad para un
pedido. La ruta principal usa optimistic locking mediante `@Version`. También
existe una ruta explícita con `?pessimistic=true` para estudiar
`PESSIMISTIC_WRITE`.

El laboratorio de consultas de `order-service` compara cuatro formas de leer
pedidos y líneas:

- N+1 intencionado.
- JPQL `fetch join`.
- `@EntityGraph`.
- DTO projection mediante constructor JPQL.

## Concurrencia de stock

La entidad JPA `ProductStockJpaEntity` conserva un número de versión. Dos
transacciones que leen la misma versión intentan actualizarla. La primera
actualización gana; la segunda recibe un conflicto y no puede dejar el stock
negativo.

La reserva pesimista usa `PESSIMISTIC_WRITE`, que bloquea la fila mientras dura
la transacción. Evita el conflicto optimista, pero puede provocar esperas y
debe utilizar transacciones cortas.

## Prueba local

Si el volumen de PostgreSQL ya existía antes de añadir `inventory_db`, crea el
usuario y la base una vez:

```powershell
docker exec distributed-orders-postgres psql -U orders -d postgres -c "CREATE USER inventory WITH PASSWORD 'inventory';"
docker exec distributed-orders-postgres psql -U orders -d postgres -c "CREATE DATABASE inventory_db OWNER inventory;"
```

Arranca el servicio:

```powershell
.\mvnw.cmd -pl services/inventory-service spring-boot:run
```

El endpoint de reserva es:

```text
POST http://localhost:8081/api/v1/stock/reservations
```

Body de ejemplo:

```json
{
  "orderId": "ad2f3742-a4a6-4b9c-aed1-45884cc7bd21",
  "productId": "55db5caf-b0da-49ab-b346-b7581a91ee76",
  "quantity": 1
}
```

Para seleccionar la alternativa pesimista:

```text
POST http://localhost:8081/api/v1/stock/reservations?pessimistic=true
```

Los tests de concurrencia preparan directamente un producto con una unidad,
lanzan dos transacciones y comprueban que solo una reserva termina bien.

## N+1

`OrderQueryLabService.loadWithNPlusOne()` carga los pedidos y después accede a
`getLines()` dentro de un bucle. Con `LAZY`, Hibernate ejecuta una consulta para
los pedidos y otra por cada colección de líneas.

Las alternativas se encuentran en el mismo servicio:

- `loadWithFetchJoin()` concentra la lectura con JPQL.
- `loadWithEntityGraph()` deja la consulta separada de la estrategia de fetch.
- `loadWithDtoProjection()` selecciona solo las columnas necesarias.

El test usa Hibernate Statistics para comparar el número real de sentencias.
No se afirma ningún resultado de rendimiento fuera de esa ejecución.

## Qué aprendí

Optimistic locking es adecuado cuando los conflictos son poco frecuentes y no
queremos bloquear lecturas. Pessimistic locking puede ser útil cuando el
conflicto es esperado, pero el coste de esperar una fila o provocar un deadlock
debe ser explícito.

N+1 no se arregla activando `EAGER` indiscriminadamente. La solución depende de
la consulta y de los datos que necesita cada caso de uso.

## Limitaciones

- `OrderCreated` ya se guarda en un transactional outbox, pero todavía no se
  publica en Kafka ni lo consume inventario.
- La reserva actual cubre un producto por operación; el flujo de varias líneas
  se coordinará en fases posteriores.
- La ruta de laboratorio de consultas todavía no es un endpoint público.
