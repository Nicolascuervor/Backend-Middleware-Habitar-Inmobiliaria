# Backend-Middleware-Habitar-Inmobiliaria
API Backend para la gestión de llamadas API entre servicios de información como HubSpot y Wasi

## Vitrina (`GET /api/v1/vitrina/{token}`)

- **`totalInmuebles`**: entero no negativo con el total de inmuebles efectivamente retornados en `inmuebles`.
- **Validación de integridad**: antes de responder, backend compara el total extraído (`totalInmuebles`) contra el total esperado en HubSpot (`listings_alquiler_filled_count + listings_venta_filled_count`).
- **Reintentos automáticos**: si no coincide, reintenta extracción con backoff incremental.
- **Fallo controlado**: si agota reintentos sin coincidencia, responde **422 Unprocessable Entity** con mensaje de desajuste de conteos.

Si en el futuro la vitrina se pagina, el contrato evolucionaría: `totalInmuebles` sería el total lógico del recurso y `inmuebles` la página actual, junto con metadatos de paginación estándar.

## Asesores (`GET /api/v1/asesores/mis-clientes`)

- Parámetro obligatorio **`hubspotOwnerId`**: mismo identificador de propietario en HubSpot que antes se obtenía del JWT tras login.
- Ya no existe **`POST /api/v1/auth/login`** ni JWT en este servicio.

## Histórico en Supabase (PostgreSQL)

La persistencia del histórico se prepara para usar Supabase/PostgreSQL.

- Endpoint de verificación de conexión: **`GET /api/v1/historico-inmuebles/db-check`**
  - Respuesta esperada: `{ "ok": true, "db": "postgres", "proveedorObjetivo": "supabase" }`.
- Script SQL de referencia para crear tabla/índices:
  - [`middleware-service/supabase/historico_inmubles.sql`](middleware-service/supabase/historico_inmubles.sql)

### Variables necesarias

- `DATABASE_URL` (recomendado): `jdbc:postgresql://<host>:<port>/<db>?sslmode=require`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`

Alternativa parametrizada en `dev` (si no se pasa `DATABASE_URL`):
- `SUPABASE_DB_HOST`
- `SUPABASE_DB_PORT` (pooler suele usar 6543, directo suele usar 5432)
- `SUPABASE_DB_NAME`
- `SUPABASE_DB_USER`
- `SUPABASE_DB_PASSWORD`

### Corte operativo y rollback

1. Configurar variables Supabase en el entorno de despliegue.
2. Desplegar backend y validar:
   - `GET /api/v1/historico-inmuebles/db-check`
   - flujo de creación/consulta de histórico.
3. Monitorear logs de conexión y tiempos de respuesta durante la primera ventana operativa.
4. Rollback rápido: restaurar variables anteriores de datasource y redeploy.
