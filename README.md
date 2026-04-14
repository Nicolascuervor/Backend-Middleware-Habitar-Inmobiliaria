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
