# Backend-Middleware-Habitar-Inmobiliaria
API Backend para la gestión de llamadas API entre servicios de información como HubSpot y Wasi

## Vitrina (`GET /api/v1/vitrina/{token}`)

- **`totalInmuebles`**: entero no negativo con el total de listings con URL válida (alquiler + venta) que esta respuesta debe cubrir para ese token. No es un “histórico global”; es el tamaño lógico de la vitrina en esta llamada.
- **Coherencia (sin paginación)**: si la respuesta es **200 OK**, se garantiza `totalInmuebles == inmuebles.length`.
- **Respuesta incompleta**: si por timeouts u orígenes externos no se pudo armar la lista completa, el backend responde **503 Service Unavailable** con el mismo cuerpo JSON (`inmuebles.length < totalInmuebles`, `alertas` con detalle). El cliente puede reintentar hasta obtener 200.

Si en el futuro la vitrina se pagina, el contrato evolucionaría: `totalInmuebles` sería el total lógico del recurso y `inmuebles` la página actual, junto con metadatos de paginación estándar.
