# Solución Nequi – Patrón Outbox

Documentación personal sobre la implementación del **Transactional Outbox Pattern** en la solución de servicios Nequi (Branch, Franchise, Product).

---

## Diagrama de flujo entre módulos



**Flujo resumido:**

1. **Cliente** llama a los APIs REST de cada servicio (Franchise 8080, Branch 8081, Product 8083).
2. Cada **servicio** persiste en **PostgreSQL** (misma BD `main_nequi_db`, schemas `franchise_db`, `branch_db`, `product_db`) y escribe en su tabla **outbox_events**.
3. Un **poller** por servicio lee eventos no publicados del outbox y los envía a **RabbitMQ** (exchange `order-exchange`, routing keys `branch.#` / `product.#`).
4. **Branch** y **Product** consumen mensajes de sus colas (`queue-branch`, `queue-product`) para reaccionar a eventos de otros servicios.

---

## 1. ¿Por qué Outbox?

Publicar un evento en RabbitMQ y persistir el agregado en base de datos **no son atómicos**. Si la aplicación falla entre ambos pasos:

- Se puede **perder** el evento (se guardó en BD pero no se publicó).
- O intentar **republicar** y causar duplicados o inconsistencia.

El patrón Outbox evita esto guardando el “evento a publicar” en la **misma unidad de trabajo** que el agregado, y dejando que un proceso asíncrono se encargue de enviar al broker y marcar como publicados.

---

## 2. Idea general

1. **Escritura:** En la misma operación (o cadena reactiva) se persisten el **agregado** (franchise, branch, product) y un **registro outbox** con el evento (payload JSON, tipo, etc.).
2. **Publicación diferida:** Un **poller** (p. ej. cada N segundos) lee registros outbox con `published = false`, los publica en RabbitMQ y, si la publicación es exitosa, los marca como `published = true`.
3. **Reintentos:** Si RabbitMQ no está disponible, el evento no se marca como publicado y se reintentará en el siguiente ciclo.

Así, la consistencia “agregado + evento” queda en BD; la entrega al broker es eventual y reintentable.

---

## 3. Esquema de la tabla `outbox_events`

Cada servicio tiene su propio schema y su tabla outbox (p. ej. `franchise_db`, `branch_db`, `product_db`):

```sql
CREATE TABLE <schema>.outbox_events (
    id           UUID PRIMARY KEY,
    aggregate_id UUID         NOT NULL,
    event_type   VARCHAR(100) NOT NULL,
    payload      JSONB        NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT now(),
    published    BOOLEAN               DEFAULT FALSE
);

CREATE INDEX idx_outbox_unpublished ON <schema>.outbox_events (published, created_at)
    WHERE published = false;
```

- **aggregate_id:** ID del agregado que generó el evento (franchise, branch, product).
- **event_type:** Tipo del evento (p. ej. para deserializar o enrutar).
- **payload:** Evento serializado en JSON (JSONB en PostgreSQL).
- **published:** `false` hasta que se publique en RabbitMQ con éxito.

---

## 4. Flujo en la solución

### 4.1 Escritura (crear/actualizar agregado + outbox)

El flujo es el mismo en los tres servicios (aquí el ejemplo con **Franchise**):

```
POST /api/v1/franchises  (o actualización de nombre)
        │
        ▼
  FranchiseHandler  →  CreateFranchiseUseCase / UpdateNameUseCase
        │
        ▼
  FranchiseRepository.save(franchise)
        │
        ▼
  FranchiseR2dbcAdapter.save()
        ├── crudRepository.save(franchise)     → tabla franchises
        └── outboxR2dbcCrudRepository.save()  → tabla outbox_events (mismo flujo reactivo)
```

En el adapter se hace algo equivalente a:

1. Guardar la entidad (franchise, branch o product).
2. Construir el evento de dominio y serializarlo a JSON.
3. Mapear a `OutboxEventEntity` (id, aggregate_id, event_type, payload, published=false).
4. Guardar la entidad outbox en la misma cadena `flatMap`/`then`, de forma que si falla la persistencia no se considera “guardado” el agregado sin su outbox.

No se publica a RabbitMQ aquí; solo se persiste el outbox.

### 4.2 Publicación periódica (poller)

```
ReactiveOutboxPublisher (@PostConstruct)
        │
        ▼
  Flux.interval(cada N segundos)
        │
        ▼
  PublishPendingEventsUseCase.execute(batchSize)
        │
        ├── OutboxRepository.findUnpublished(batchSize)
        ├── Por cada evento: DomainEventPublisher.publish(event)  → RabbitMQ
        └── Si publish OK: OutboxRepository.markAsPublished(id)
```

- **ReactiveOutboxPublisher:** programa el intervalo (`app.outbox.polling-interval-seconds`) y el tamaño de lote (`app.outbox.batch-size`).
- **PublishPendingEventsUseCase:** obtiene eventos no publicados, los envía al broker y solo entonces marca como publicados (en franchise se valida el resultado del publish antes de marcar).

---

## 5. Componentes por capa (referencia)

| Capa        | Componente                  | Rol |
|------------|-----------------------------|-----|
| **Domain** | `OutboxEvent`               | Modelo del evento outbox (id, event, etc.). |
| **Domain** | `OutboxRepository`          | Puerto: `findUnpublished(batchSize)`, `markAsPublished(id)`. |
| **Domain** | `DomainEventPublisher`      | Puerto para publicar en el broker. |
| **Use case** | `PublishPendingEventsUseCase` | Orquesta: leer outbox → publicar → marcar. |
| **Infra**  | `OutboxEventEntity` / `OutboxR2dbcCrudRepository` | Persistencia outbox (R2DBC). |
| **Infra**  | `FranchiseR2dbcAdapter` (y equivalentes Branch/Product) | Guarda agregado + outbox en la misma cadena reactiva. |
| **Infra**  | `RabbitMQEventPublisher`    | Implementación de `DomainEventPublisher`. |
| **Entry**  | `ReactiveOutboxPublisher`   | Scheduler que dispara el use case de publicación. |

---

## 6. Configuración (application.yaml)

```yaml
app:
  outbox:
    polling-interval-seconds: 2   # Cada cuántos segundos se revisa el outbox
    batch-size: 50                # Máximo de eventos por ciclo
```

Cada servicio (branch, franchise, product) puede definir estos valores según necesidad.

---

## 7. Resumen

- **Outbox** = tabla donde se guardan “eventos pendientes de publicar” en la **misma unidad lógica** que el agregado.
- **Poller** = proceso que periódicamente lee `published = false`, publica en RabbitMQ y marca como publicados.
- **Ventajas:** no se pierden eventos por fallos entre BD y broker; se evita publicar sin haber persistido; reintentos automáticos si el broker no está disponible.
- **Trade-off:** la entrega al broker es **eventual** (depende del intervalo de polling y del estado de RabbitMQ).

Este README refleja la implementación actual en los servicios **branch**, **franchise** y **product** de la solución Nequi.

---

## 8. Levantamiento en forma local

### Requisitos

- **Java 21**
- **Gradle** (o usar el wrapper `gradlew` de cada proyecto)
- **Docker y Docker Compose** (para PostgreSQL y RabbitMQ)

### Opción A: Solo infraestructura en Docker y servicios con Gradle

Útil para desarrollar y depurar cada servicio desde el IDE.

1. **Levantar solo PostgreSQL y RabbitMQ** (desde la raíz de `companies/nequi`):

   ```bash
   cd companies/nequi
   docker compose up -d postgres rabbitmq
   ```

2. **Esperar a que estén listos** (healthchecks). PostgreSQL crea la BD `main_nequi_db` y los schemas al iniciar por primera vez.

3. **Levantar cada servicio por separado** (cada uno en su carpeta, con su `application.yaml` apuntando a `localhost`):

   ```bash
   # Terminal 1 – Franchise (puerto 8080)
   cd franchise && ./gradlew :app-service:bootRun

   # Terminal 2 – Branch (puerto 8081)
   cd branch && ./gradlew :app-service:bootRun

   # Terminal 3 – Product (puerto 8083)
   cd product && ./gradlew :app-service:bootRun
   ```

   O ejecutar la clase `MainApplication` de cada `app-service` desde el IDE.

4. **Puertos y URLs:**

   | Servicio   | Puerto | API base          | Swagger (si aplica)   |
   |-----------|--------|-------------------|------------------------|
   | Franchise | 8080   | http://localhost:8080 | http://localhost:8080/swagger-ui.html |
   | Branch    | 8081   | http://localhost:8081 | http://localhost:8081/swagger-ui.html |
   | Product   | 8083   | http://localhost:8083 | http://localhost:8083/swagger-ui.html |
   | RabbitMQ  | 5672 (AMQP), 15672 (Management) | — | http://localhost:15672 (guest/guest) |
   | PostgreSQL | 5432  | —                 | Conexión: `localhost:5432`, DB: `main_nequi_db`, user: `postgres` |

### Opción B: Todo con Docker Compose

Levanta infraestructura y los tres servicios en contenedores (imágenes ARM64).

```bash
cd companies/nequi
docker compose up -d --build
```

- **Franchise:** http://localhost:8080  
- **Branch:** http://localhost:8081  
- **Product:** http://localhost:8083  
- **RabbitMQ Management:** http://localhost:15672 (guest/guest)  
- **PostgreSQL:** `localhost:5432`, base `main_nequi_db`, usuario `postgres`, contraseña `postgres`

Para ver logs de un servicio:

```bash
docker compose logs -f franchise-service
```

### Conexión a base de datos en local

Con `application.yaml` por defecto (sin variables de entorno), los servicios esperan:

- **Host:** `localhost`
- **Puerto:** `5432`
- **Base de datos:** `main_nequi_db` (o la que tengas configurada; en Docker se crea con los schemas `franchise_db`, `branch_db`, `product_db`)
- **Usuario / contraseña:** según tu `application.yaml` (p. ej. `postgres` / `postgres`)

Si usas **Opción A**, deja las properties con `localhost`. Si usas **Opción B**, la configuración se sobrescribe por las variables de entorno del `docker-compose`.

### Terraform

Si quieres probar la infraestructura en AWS (RDS, RabbitMQ), puedes usar el módulo Terraform incluido en `companies/nequi/infra` para crear los recursos necesarios. Asegúrate de configurar tus credenciales de AWS y ajustar las variables según tu entorno.

```bash
# 1. Inicializar
terraform init

# 2. Ver qué va a crear
terraform plan -var="github_repo=https://github.com/tu-usuario/tu-repo.git"

# 3. Desplegar
terraform apply -var="github_repo=https://github.com/tu-usuario/tu-repo.git"

# Para repo privado:
terraform apply \
  -var="github_repo=https://github.com/tu-usuario/tu-repo.git" \
  -var="github_token=ghp_tuTokenAqui"

# 4. ¡IMPORTANTE! Cuando termines, destruir todo
terraform destroy -var="github_repo=https://github.com/..."
```   

