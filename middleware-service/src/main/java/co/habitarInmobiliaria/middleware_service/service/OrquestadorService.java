package co.habitarinmobiliaria.middleware_service.service;

import co.habitarinmobiliaria.middleware_service.constans.HubSpotConstants;
import co.habitarinmobiliaria.middleware_service.dtos.ListingItemDTO;
import co.habitarinmobiliaria.middleware_service.dtos.hubspot.HubSpotContactDTO;
import co.habitarinmobiliaria.middleware_service.dtos.hubspot.HubSpotOwnerDTO;
import co.habitarinmobiliaria.middleware_service.dtos.wasi.WasiInmuebleDTO;
import co.habitarinmobiliaria.middleware_service.strategy.EstadoInmuebleStrategy;
import co.habitarinmobiliaria.middleware_service.client.AirtableClient;
import co.habitarinmobiliaria.middleware_service.client.HubSpotClient;
import co.habitarinmobiliaria.middleware_service.client.WasiClient;
import co.habitarinmobiliaria.middleware_service.dtos.*;
import co.habitarinmobiliaria.middleware_service.exception.RecursoNoEncontradoException;
import co.habitarinmobiliaria.middleware_service.util.LogSanitizer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

@Service
@Slf4j
public class OrquestadorService {

    private final List<EstadoInmuebleStrategy> estrategias;
    private final HubSpotClient hubSpotClient;
    private final WasiClient wasiClient;
    private final InmuebleMapperService mapperService;
    private final AirtableClient airtableClient;
    private final ObjectMapper objectMapper;
    private final InmueblePrivadoService inmueblePrivadoService;
    private final AsesorCacheService asesorCacheService;
    private final Executor executor;

    @Value("${airtable.token}")
    private String airtableToken;

    @Value("${airtable.table.inmuebles}")
    private String tableName;

    @Value("${asesor.n.id}") private String idN;
    @Value("${asesor.n.foto}") private String fotoN;
    @Value("${asesor.n.tel}") private String telN;
    @Value("${asesor.n.meet}") private String meetN;

    @Value("${asesor.s.id}") private String idS;
    @Value("${asesor.s.foto}") private String fotoS;
    @Value("${asesor.s.tel}") private String telS;
    @Value("${asesor.s.meet}") private String meetS;

    @Value("${asesor.j.id}") private String idJ;
    @Value("${asesor.j.foto}") private String fotoJ;
    @Value("${asesor.j.tel}") private String telJ;
    @Value("${asesor.j.meet}") private String meetJ;

    @Value("${asesor.d.id}") private String idD;
    @Value("${asesor.d.foto}") private String fotoD;
    @Value("${asesor.d.tel}") private String telD;
    @Value("${asesor.d.meet}") private String meetD;

    @Value("${asesor.m.id}") private String idM;
    @Value("${asesor.m.foto}") private String fotoM;
    @Value("${asesor.m.tel}") private String telM;
    @Value("${asesor.m.meet}") private String meetM;

    @Value("${asesor.a.id}") private String idA;
    @Value("${asesor.a.foto}") private String fotoA;
    @Value("${asesor.a.tel}") private String telA;
    @Value("${asesor.a.meet}") private String meetA;

    @Value("${asesor.js.id}") private String idJS;
    @Value("${asesor.js.foto}") private String fotoJS;
    @Value("${asesor.js.tel}") private String telJS;
    @Value("${asesor.js.meet}") private String meetJS;

    // Constructor explícito para inyectar el Executor cualificado
    public OrquestadorService(
            List<EstadoInmuebleStrategy> estrategias,
            HubSpotClient hubSpotClient,
            WasiClient wasiClient,
            InmuebleMapperService mapperService,
            AirtableClient airtableClient,
            ObjectMapper objectMapper,
            InmueblePrivadoService inmueblePrivadoService,
            AsesorCacheService asesorCacheService,
            @Qualifier("vitrinaExecutor") Executor executor) {
        this.estrategias = estrategias;
        this.hubSpotClient = hubSpotClient;
        this.wasiClient = wasiClient;
        this.mapperService = mapperService;
        this.airtableClient = airtableClient;
        this.objectMapper = objectMapper;
        this.inmueblePrivadoService = inmueblePrivadoService;
        this.asesorCacheService = asesorCacheService;
        this.executor = executor;
    }

    // ─── Propiedades a solicitar a HubSpot ──────────────────────────────────
    private static final String PROPS_VITRINA =
            HubSpotConstants.FIRSTNAME + "," +
                    HubSpotConstants.OWNER_ID + "," +
                    HubSpotConstants.LISTINGS_ALQUILER_DATA + "," +
                    HubSpotConstants.LISTINGS_VENTA_DATA;

    // ─── Utilidades JSON ─────────────────────────────────────────────────────

    private List<ListingItemDTO> parsearListings(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<ListingItemDTO>>() {});
        } catch (Exception e) {
            log.warn("No se pudo parsear listings JSON: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private String serializarListings(List<ListingItemDTO> listings) {
        try {
            return objectMapper.writeValueAsString(listings);
        } catch (Exception e) {
            log.error("Error serializando listings: {}", e.getMessage());
            return "[]";
        }
    }

    // ─── procesarVitrina (OPTIMIZADO: paralelo + cacheable) ─────────────────

    @Cacheable(value = "vitrina", key = "#usuarioToken")
    public VitrinaResponseDTO procesarVitrina(String usuarioToken) {
        log.info("Iniciando orquestación completa para usuario: {}", usuarioToken);

        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken, PROPS_VITRINA);

        if (contacto == null || contacto.getProperties() == null) {
            log.warn("Usuario no encontrado o sin propiedades: {}", usuarioToken);
            return VitrinaResponseDTO.builder()
                    .asesor(construirInfoAsesor(null))
                    .inmuebles(new ArrayList<>())
                    .alertas(new ArrayList<>())
                    .build();
        }

        Map<String, String> dinamicas = contacto.getProperties().getPropiedadesDinamicas();

        List<ListingItemDTO> alquiler = parsearListings(dinamicas.get(HubSpotConstants.LISTINGS_ALQUILER_DATA));
        List<ListingItemDTO> venta    = parsearListings(dinamicas.get(HubSpotConstants.LISTINGS_VENTA_DATA));

        // Filtrar listings con URL válida
        List<ListingItemDTO> listingsActivos = Stream.concat(alquiler.stream(), venta.stream())
                .filter(item -> item.getUrl() != null && !item.getUrl().isBlank())
                .toList();

        // Colector thread-safe para alertas de degradación parcial
        List<String> alertas = new CopyOnWriteArrayList<>();

        // ══════════════════════════════════════════════════════════════════
        // OPTIMIZACIÓN CRÍTICA: Ejecución PARALELA de consultas externas
        // ANTES: N llamadas seriales × 3-5s = 30-60s TTFB
        // DESPUÉS: N llamadas paralelas = max(3-5s) = 3-8s TTFB
        // ══════════════════════════════════════════════════════════════════
        List<CompletableFuture<VitrinaInmuebleDTO>> futures = listingsActivos.stream()
                .map(item -> CompletableFuture.supplyAsync(
                        () -> consultarInmuebleIndividual(item.getUrl(), item.getEstado()),
                        executor
                ).exceptionally(ex -> {
                    log.warn("Fallo al consultar inmueble {}: {}", item.getUrl(), ex.getMessage());
                    alertas.add("No se pudo obtener datos del inmueble: " + item.getUrl());
                    return null;
                }))
                .toList();

        // Obtener info del asesor EN PARALELO con las consultas de inmuebles
        String ownerId = contacto.getProperties().getOwnerId();
        CompletableFuture<VitrinaResponseDTO.AsesorInfo> asesorFuture =
                CompletableFuture.supplyAsync(() -> construirInfoAsesor(ownerId), executor);

        // Esperar a que TODAS las consultas de inmuebles terminen
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<VitrinaInmuebleDTO> vitrinaFinal = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();

        // El asesor también debería estar listo a esta altura
        VitrinaResponseDTO.AsesorInfo asesorInfo = asesorFuture.join();

        return VitrinaResponseDTO.builder()
                .asesor(asesorInfo)
                .inmuebles(vitrinaFinal)
                .alertas(alertas.isEmpty() ? null : new ArrayList<>(alertas))
                .build();
    }

    // ─── procesarCambioEstado ────────────────────────────────────────────────

    public void procesarCambioEstado(String usuarioToken, String urlRecibida, String accion) {
        log.info("Procesando cambio de estado: {} para URL: {}",
                LogSanitizer.sanitizar(accion), LogSanitizer.sanitizar(urlRecibida));

        EstadoInmuebleStrategy estrategia = estrategias.stream()
                .filter(s -> s.aplicaPara(accion))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Acción no permitida: " + accion));

        String idTarget = mapperService.extraerIdDeUrl(urlRecibida);
        if (idTarget == null) throw new IllegalArgumentException("No se pudo extraer un ID válido de la URL");

        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken, PROPS_VITRINA);
        if (contacto == null || contacto.getProperties() == null)
            throw new RecursoNoEncontradoException("No se encontró el contacto en HubSpot");

        Map<String, String> dinamicas = contacto.getProperties().getPropiedadesDinamicas();

        // Intentar en alquiler primero, luego venta
        if (actualizarEstadoEnLista(usuarioToken, dinamicas,
                HubSpotConstants.LISTINGS_ALQUILER_DATA, idTarget, estrategia.getNombre())) return;

        if (actualizarEstadoEnLista(usuarioToken, dinamicas,
                HubSpotConstants.LISTINGS_VENTA_DATA, idTarget, estrategia.getNombre())) return;

        throw new RecursoNoEncontradoException("El inmueble no pertenece a la vitrina de este cliente");
    }

    private boolean actualizarEstadoEnLista(String token, Map<String, String> dinamicas,
                                            String propiedad, String idTarget, String nuevoEstado) {
        List<ListingItemDTO> listings = parsearListings(dinamicas.get(propiedad));
        for (ListingItemDTO item : listings) {
            if (idTarget.equals(mapperService.extraerIdDeUrl(item.getUrl()))) {
                item.setEstado(nuevoEstado);
                actualizarEnHubSpot(token, propiedad, serializarListings(listings));
                log.info("Estado actualizado en {} para ID {}: {}", propiedad, idTarget, nuevoEstado);
                return true;
            }
        }
        return false;
    }

    // ─── obtenerInmuebleEspecifico ───────────────────────────────────────────

    public InmuebleDetalleDTO obtenerInmuebleEspecifico(String usuarioToken, String parametroRecibido) {
        String inmuebleId = mapperService.extraerIdDeUrl(parametroRecibido);
        if (inmuebleId == null) throw new IllegalArgumentException("ID de inmueble no válido");

        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken, PROPS_VITRINA);
        String estadoActual = "SIN_REVISAR";

        if (contacto != null && contacto.getProperties() != null) {
            Map<String, String> dinamicas = contacto.getProperties().getPropiedadesDinamicas();

            List<ListingItemDTO> todos = new ArrayList<>();
            todos.addAll(parsearListings(dinamicas.get(HubSpotConstants.LISTINGS_ALQUILER_DATA)));
            todos.addAll(parsearListings(dinamicas.get(HubSpotConstants.LISTINGS_VENTA_DATA)));

            estadoActual = todos.stream()
                    .filter(item -> inmuebleId.equals(mapperService.extraerIdDeUrl(item.getUrl())))
                    .map(item -> item.getEstado() != null ? item.getEstado() : "SIN_REVISAR")
                    .findFirst()
                    .orElse("SIN_REVISAR");
        }

        if (inmuebleId.startsWith("rec")) {
            log.info("Enrutando detalle a Airtable para ID: {}", inmuebleId);
            String tokenFormateado = "Bearer " + airtableToken;
            com.fasterxml.jackson.databind.JsonNode airtableRecord =
                    airtableClient.obtenerRegistro(tokenFormateado, tableName, inmuebleId);
            return mapperService.mapAirtableToDetalle(airtableRecord, estadoActual);
        } else if (inmuebleId.matches("^[a-zA-Z0-9]{8}$") && !inmuebleId.matches("^\\d+$")) {
            log.info("Enrutando detalle a HubSpot para ID: {}", inmuebleId);
            Map<String, Object> map = inmueblePrivadoService.obtenerInmueblePorCodigo(inmuebleId);
            if (map == null) throw new RecursoNoEncontradoException("Inmueble privado no encontrado en HubSpot");
            return mapperService.mapHubSpotToDetalle(map, estadoActual);
        } else if (inmuebleId.matches("^\\d+$")) {
            log.info("Enrutando detalle a Wasi para ID: {}", inmuebleId);
            try {
                WasiInmuebleDTO inmuebleRaw = wasiClient.obtenerInmueblePorId(inmuebleId);
                if (inmuebleRaw != null) {
                    return mapperService.mapToDetalle(inmuebleRaw, estadoActual);
                }
            } catch (Exception e) {
                if (inmuebleId.length() == 8) {
                    log.info("Fallo en Wasi, intentando como Inmueble Privado HubSpot (ID 100% numérico): {}", inmuebleId);
                    try {
                        Map<String, Object> map = inmueblePrivadoService.obtenerInmueblePorCodigo(inmuebleId);
                        if (map != null) return mapperService.mapHubSpotToDetalle(map, estadoActual);
                    } catch (Exception ex) {
                        throw new RecursoNoEncontradoException("El inmueble con ID " + inmuebleId + " no existe.");
                    }
                }
                throw new RecursoNoEncontradoException("El inmueble con ID " + inmuebleId + " no existe en Wasi.");
            }
        }
        throw new IllegalArgumentException("ID de inmueble no soportado: " + inmuebleId);
    }

    // ─── asignarInmuebleAutomaticamente ─────────────────────────────────────

    @CacheEvict(value = "vitrina", key = "#usuarioToken")
    public void asignarInmuebleAutomaticamente(String usuarioToken, String urlWasi, String tipoInmueble) {
        log.info("Intento de asignar inmueble [{}] para usuario: {}", tipoInmueble, usuarioToken);

        String nuevoId = mapperService.extraerIdDeUrl(urlWasi);
        if (nuevoId == null) throw new IllegalArgumentException("URL inválida.");

        boolean esAlquiler = "ALQUILER".equalsIgnoreCase(tipoInmueble);
        String propiedad = esAlquiler
                ? HubSpotConstants.LISTINGS_ALQUILER_DATA
                : HubSpotConstants.LISTINGS_VENTA_DATA;

        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken, PROPS_VITRINA);
        if (contacto == null || contacto.getProperties() == null)
            throw new RecursoNoEncontradoException("Usuario no encontrado");

        Map<String, String> dinamicas = contacto.getProperties().getPropiedadesDinamicas();
        List<ListingItemDTO> listings = parsearListings(dinamicas.get(propiedad));

        // Verificar duplicado
        boolean duplicado = listings.stream()
                .anyMatch(item -> nuevoId.equals(mapperService.extraerIdDeUrl(item.getUrl())));
        if (duplicado) throw new IllegalStateException("El inmueble " + nuevoId + " ya está asignado.");

        listings.add(new ListingItemDTO(urlWasi, "SIN_REVISAR"));
        actualizarEnHubSpot(usuarioToken, propiedad, serializarListings(listings));
        log.info("Inmueble {} asignado correctamente en {}", nuevoId, propiedad);
    }

    // ─── desasignarInmueble ──────────────────────────────────────────────────

    @CacheEvict(value = "vitrina", key = "#usuarioToken")
    public void desasignarInmueble(String usuarioToken, String urlRecibida) {
        log.info("Intento de desasignar inmueble para usuario: {}", usuarioToken);

        String idTarget = mapperService.extraerIdDeUrl(urlRecibida);
        if (idTarget == null) throw new IllegalArgumentException("URL inválida.");

        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken, PROPS_VITRINA);
        if (contacto == null || contacto.getProperties() == null)
            throw new RecursoNoEncontradoException("Usuario no encontrado");

        Map<String, String> dinamicas = contacto.getProperties().getPropiedadesDinamicas();

        if (desasignarDeLista(usuarioToken, dinamicas, HubSpotConstants.LISTINGS_ALQUILER_DATA, idTarget)) return;
        if (desasignarDeLista(usuarioToken, dinamicas, HubSpotConstants.LISTINGS_VENTA_DATA, idTarget)) return;

        throw new RecursoNoEncontradoException("El inmueble no fue encontrado en la vitrina de este cliente.");
    }

    private boolean desasignarDeLista(String token, Map<String, String> dinamicas,
                                      String propiedad, String idTarget) {
        List<ListingItemDTO> listings = parsearListings(dinamicas.get(propiedad));
        boolean removido = listings.removeIf(
                item -> idTarget.equals(mapperService.extraerIdDeUrl(item.getUrl())));
        if (removido) {
            actualizarEnHubSpot(token, propiedad, serializarListings(listings));
            log.info("Inmueble {} desasignado de {}", idTarget, propiedad);
        }
        return removido;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private VitrinaInmuebleDTO consultarInmuebleIndividual(String url, String estado) {
        String inmuebleId = mapperService.extraerIdDeUrl(url);
        if (inmuebleId == null) return null;
        String estadoFinal = estado != null ? estado : "SIN_REVISAR";
        try {
            if (inmuebleId.startsWith("rec")) {
                log.info("Vitrina - Consultando Airtable para ID: {}", inmuebleId);
                String tokenFormateado = "Bearer " + airtableToken;
                com.fasterxml.jackson.databind.JsonNode airtableRecord =
                        airtableClient.obtenerRegistro(tokenFormateado, tableName, inmuebleId);
                if (airtableRecord != null && airtableRecord.has("fields")) {
                    return mapperService.mapAirtableToVitrina(airtableRecord, estadoFinal, url);
                }
            } else if (inmuebleId.matches("^[a-zA-Z0-9]{8}$") && !inmuebleId.matches("^\\d+$")) {
                log.info("Vitrina - Consultando HubSpot para ID: {}", inmuebleId);
                Map<String, Object> map = inmueblePrivadoService.obtenerInmueblePorCodigo(inmuebleId);
                if (map != null) {
                    return mapperService.mapHubSpotToVitrina(map, estadoFinal, url);
                }
            } else if (inmuebleId.matches("^\\d+$")) {
                log.info("Vitrina - Consultando Wasi para ID: {}", inmuebleId);
                try {
                    WasiInmuebleDTO inmuebleRaw = wasiClient.obtenerInmueblePorId(inmuebleId);
                    if (inmuebleRaw != null) {
                        return mapperService.mapToVitrina(inmuebleRaw, estadoFinal, url);
                    }
                } catch (Exception e) {
                    if (inmuebleId.length() == 8) {
                        log.info("Vitrina - Fallo en Wasi, consultando HubSpot para ID 100% numérico: {}", inmuebleId);
                        try {
                            Map<String, Object> map = inmueblePrivadoService.obtenerInmueblePorCodigo(inmuebleId);
                            if (map != null) return mapperService.mapHubSpotToVitrina(map, estadoFinal, url);
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error obteniendo inmueble {}: {}", inmuebleId, e.getMessage());
        }
        return null;
    }

    private void actualizarEnHubSpot(String token, String campo, String valor) {
        Map<String, String> prop = Collections.singletonMap(campo, valor);
        hubSpotClient.actualizarContacto(token, Collections.singletonMap("properties", prop));
    }

    private VitrinaResponseDTO.AsesorInfo construirInfoAsesor(String ownerId) {
        log.info("Iniciando mapeo para OwnerId: [{}]", ownerId);

        if (ownerId == null || ownerId.trim().isEmpty()) {
            return VitrinaResponseDTO.AsesorInfo.builder()
                    .nombreCompleto("Asesor No Asignado")
                    .fotoUrl("https://via.placeholder.com/200?text=Sin+Foto")
                    .build();
        }

        try {
            // Usa AsesorCacheService para cachear la llamada a HubSpot
            HubSpotOwnerDTO owner = asesorCacheService.obtenerAsesor(ownerId);
            String urlFoto, telefonoContacto, urlMeeting;

            if (ownerId.equals(idN)) { urlFoto = fotoN; telefonoContacto = telN; urlMeeting = meetN; }
            else if (ownerId.equals(idS)) { urlFoto = fotoS; telefonoContacto = telS; urlMeeting = meetS; }
            else if (ownerId.equals(idJ)) { urlFoto = fotoJ; telefonoContacto = telJ; urlMeeting = meetJ; }
            else if (ownerId.equals(idD)) { urlFoto = fotoD; telefonoContacto = telD; urlMeeting = meetD; }
            else if (ownerId.equals(idM)) { urlFoto = fotoM; telefonoContacto = telM; urlMeeting = meetM; }
            else if (ownerId.equals(idA)) { urlFoto = fotoA; telefonoContacto = telA; urlMeeting = meetA; }
            else if (ownerId.equals(idJS)) { urlFoto = fotoJS; telefonoContacto = telJS; urlMeeting = meetJS; }
            else {
                log.warn("OwnerId [{}] no coincide con ningún asesor configurado", ownerId);
                urlFoto = "https://via.placeholder.com/200?text=Asesor";
                telefonoContacto = "No disponible";
                urlMeeting = "https://habitarinmobiliaria.co/contacto";
            }

            return VitrinaResponseDTO.AsesorInfo.builder()
                    .nombreCompleto(owner.getFirstName() + " " + (owner.getLastName() != null ? owner.getLastName() : ""))
                    .correo(owner.getEmail())
                    .telefono(telefonoContacto)
                    .fotoUrl(urlFoto)
                    .linkMeeting(urlMeeting)
                    .build();

        } catch (Exception e) {
            log.error("Error crítico en construirInfoAsesor para ID {}: {}", ownerId, e.getMessage());
            return VitrinaResponseDTO.AsesorInfo.builder()
                    .nombreCompleto("Error al cargar asesor")
                    .fotoUrl("https://via.placeholder.com/200?text=Error")
                    .build();
        }
    }
}