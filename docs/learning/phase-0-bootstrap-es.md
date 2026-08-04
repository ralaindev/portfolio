# FASE 0: Bootstrap

## Qué se ha construido

El repositorio tiene un build Maven multi-module con Java 21 y un primer módulo,
`order-service`. El servicio todavía no contiene el agregado `Order`: solo puede
arrancar, conectarse a PostgreSQL y ejecutar su migración Flyway inicial.

## Conceptos

- **Multi-módulo Maven (Maven multi-module build):** el POM raíz coordina varios
  servicios sin compartir sus modelos internos.
- **Migración versionada (Versioned migration):** Flyway aplica `V1__...` una vez
  y registra su ejecución; las migraciones aplicadas no se editan.
- **Health check:** Actuator expone una señal mínima para saber si el proceso y
  sus dependencias están disponibles.
- **Prueba de contexto (Application context smoke test):** verifica que Spring
  puede construir el servicio, pero no sustituye a las pruebas de negocio.

## Prueba manual

```bash
docker compose up -d postgres
bash ./mvnw clean verify
bash ./mvnw -pl services/order-service spring-boot:run
```

En Windows, sustituye esos comandos por `mvnw.cmd clean verify` y
`mvnw.cmd -pl services/order-service spring-boot:run`.

En otra terminal:

```bash
curl http://localhost:8080/actuator/health
```

## Limitaciones

El test de contexto necesita PostgreSQL disponible porque JPA y Flyway están
configurados desde el inicio. Testcontainers y el dominio se añadirán en fases
posteriores.
