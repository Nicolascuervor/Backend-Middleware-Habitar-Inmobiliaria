package co.habitarinmobiliaria.middleware_service.service;

import co.habitarinmobiliaria.middleware_service.constans.HubSpotConstants;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrquestadorService {

    private final List<EstadoInmuebleStrategy> estrategias;
    private final HubSpotClient hubSpotClient;
    private final WasiClient wasiClient;
    private final InmuebleMapperService mapperService;
    private final AirtableClient airtableClient;

    private static final int MAX_VITRINA_SIZE = 30;

    @Value("${airtable.token}")
    private String airtableToken;

    @Value("${airtable.table.inmuebles}")
    private String tableName;

    @Value("${asesor.n.id}")
    private String idN;

    @Value("${asesor.n.foto}")
    private String fotoN;

    @Value("${asesor.n.tel}")
    private String telN;

    @Value("${asesor.n.meet}")
    private String meetN;

    /* Asesor S */
    @Value("${asesor.s.id}")
    private String idS;
    @Value("${asesor.s.foto}")
    private String fotoS;
    @Value("${asesor.s.tel}")
    private String telS;
    @Value("${asesor.s.meet}")
    private String meetS;

    /* Asesor J */
    @Value("${asesor.j.id}")
    private String idJ;
    @Value("${asesor.j.foto}")
    private String fotoJ;
    @Value("${asesor.j.tel}")
    private String telJ;
    @Value("${asesor.j.meet}")
    private String meetJ;

    /* Asesor D */
    @Value("${asesor.d.id}")
    private String idD;
    @Value("${asesor.d.foto}")
    private String fotoD;
    @Value("${asesor.d.tel}")
    private String telD;
    @Value("${asesor.d.meet}")
    private String meetD;

    /* Asesor M */
    @Value("${asesor.m.id}")
    private String idM;
    @Value("${asesor.m.foto}")
    private String fotoM;
    @Value("${asesor.m.tel}")
    private String telM;
    @Value("${asesor.m.meet}")
    private String meetM;

    /* Asesor A */
    @Value("${asesor.a.id}")
    private String idA;
    @Value("${asesor.a.foto}")
    private String fotoA;
    @Value("${asesor.a.tel}")
    private String telA;
    @Value("${asesor.a.meet}")
    private String meetA;

    /* Asesor JS */
    @Value("${asesor.js.id}")
    private String idJS;
    @Value("${asesor.js.foto}")
    private String fotoJS;
    @Value("${asesor.js.tel}")
    private String telJS;
    @Value("${asesor.js.meet}")
    private String meetJS;



    public void procesarCambioEstado(String usuarioToken, String urlRecibida, String accion) {
        log.info("Procesando cambio de estado: {} para URL: {}", LogSanitizer.sanitizar(accion), LogSanitizer.sanitizar(urlRecibida));

        /* Buscar estrategia correcta */
        EstadoInmuebleStrategy estrategia = estrategias.stream()
                .filter(s -> s.aplicaPara(accion))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Acción no permitida: " + accion));

        /* Extraer ID de la URL */
        String idTarget = mapperService.extraerIdDeUrl(urlRecibida);

        if (idTarget == null) {
            throw new IllegalArgumentException("No se pudo extraer un ID válido de la URL proporcionada");
        }

        /* Generar las propiedades a pedir a HubSpot */

        StringBuilder propiedadesSolicitadas = new StringBuilder(HubSpotConstants.FIRSTNAME);
        for (int i = 1; i <= MAX_VITRINA_SIZE; i++) {
            propiedadesSolicitadas.append(",").append(HubSpotConstants.LISTING_PREFIX).append(i);
        }

        /* Obtener contacto de HubSpot */
        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken, propiedadesSolicitadas.toString());

        if (contacto == null || contacto.getProperties() == null) {
            throw new RecursoNoEncontradoException("No se encontró el contacto en HubSpot");
        }

        /* Extraer mapa */
        Map<String, String> propiedadesActuales = contacto.getProperties().getPropiedadesDinamicas();

        /* Buscar iterando sobre la colección */
        for (int i = 1; i <= MAX_VITRINA_SIZE; i++) {
            String campoListing = HubSpotConstants.LISTING_PREFIX + i;
            String urlEnSlot = propiedadesActuales.get(campoListing);

            if (urlEnSlot != null && !urlEnSlot.trim().isEmpty()) {
                String idEnSlot = mapperService.extraerIdDeUrl(urlEnSlot);

                if (idTarget.equals(idEnSlot)) {
                    /* Quitar estado previo y aplicar nuevo usando Expresiones Regulares */
                    String urlBase = urlEnSlot.replaceAll("-[A-Z_]+$", "");
                    String urlNueva = urlBase + estrategia.getSufijo();

                    log.info("Actualizando slot {} con nueva URL: {}", campoListing, urlNueva);
                    actualizarEnHubSpot(usuarioToken, campoListing, urlNueva);
                    return; /* Finaliza la ejecución al encontrar y actualizar el objetivo */
                }
            }
        }

        /*
         * Si el bucle termina sin hacer 'return', el inmueble no estaba en la vitrina
         */
        throw new RecursoNoEncontradoException("El inmueble no pertenece a la vitrina de este cliente");
    }

    private void actualizarEnHubSpot(String token, String campo, String valor) {
        Map<String, String> prop = Collections.singletonMap(campo, valor);
        hubSpotClient.actualizarContacto(token, Collections.singletonMap("properties", prop));
    }

    public VitrinaResponseDTO procesarVitrina(String usuarioToken) {
        log.info("Iniciando orquestación completa para usuario: {}", usuarioToken);

        /* 1. Construir petición dinámica (Aplicando constantes y tamaño máximo) */
        StringBuilder propiedadesSolicitadas = new StringBuilder(HubSpotConstants.FIRSTNAME);
        propiedadesSolicitadas.append(",").append(HubSpotConstants.OWNER_ID);

        for (int i = 1; i <= MAX_VITRINA_SIZE; i++) {
            propiedadesSolicitadas.append(",").append(HubSpotConstants.LISTING_PREFIX).append(i);
            propiedadesSolicitadas.append(",").append(HubSpotConstants.LISTING_PREFIX).append(i).append("_a");
            propiedadesSolicitadas.append(",").append(HubSpotConstants.LISTING_PREFIX).append(i).append("_v");
        }

        /* Obtener datos de HubSpot */
        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken, propiedadesSolicitadas.toString());

        /* Validar contacto */
        if (contacto == null || contacto.getProperties() == null) {
            log.warn("Usuario no encontrado o sin propiedades: {}", usuarioToken);
            return VitrinaResponseDTO.builder()
                    .asesor(construirInfoAsesor(null))
                    .inmuebles(new ArrayList<>())
                    .build();
        }

        /* 2. Extraer URLs de listings (Usando constantes) */
        Map<String, String> dinamicas = contacto.getProperties().getPropiedadesDinamicas();
        log.info("LLAVES RECIBIDAS DE HUBSPOT: {}", dinamicas.keySet());
        List<String> urlsListings = dinamicas.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(HubSpotConstants.LISTING_PREFIX)
                        && entry.getValue() != null
                        && !entry.getValue().trim().isEmpty())
                .map(Map.Entry::getValue)
                .toList();

        /* 3. REFACTOR: Delegar lógica compleja y mapeo mediante Streams */
        List<VitrinaInmuebleDTO> vitrinaFinal = urlsListings.stream()
                .map(this::consultarInmuebleIndividual)
                .filter(Objects::nonNull) // Descarta los inmuebles que fallaron o no existen
                .toList();

        /* Construir info del asesor extrayéndolo directamente del contacto */
        String ownerId = contacto.getProperties().getOwnerId();
        VitrinaResponseDTO.AsesorInfo infoAsesor = construirInfoAsesor(ownerId);

        /* Retornar respuesta final */
        return VitrinaResponseDTO.builder()
                .asesor(infoAsesor)
                .inmuebles(vitrinaFinal)
                .build();
    }

    private VitrinaInmuebleDTO consultarInmuebleIndividual(String url) {
        String inmuebleId = mapperService.extraerIdDeUrl(url);
        String estadoDelInmueble = mapperService.extraerEstadoDeUrl(url);
        if (inmuebleId == null) {
            return null;
        }
        try {
            /* Ruta Airtable o Wasi según el ID */
            if (inmuebleId.startsWith("rec")) {
                log.info("Vitrina - Consultando Airtable para ID: {}", inmuebleId);
                String tokenFormateado = "Bearer " + airtableToken;
                com.fasterxml.jackson.databind.JsonNode airtableRecord = airtableClient.obtenerRegistro(
                        tokenFormateado, tableName, inmuebleId);

                if (airtableRecord != null && airtableRecord.has("fields")) {
                    return mapperService.mapAirtableToVitrina(airtableRecord, estadoDelInmueble, url);
                }
            } else {
                log.info("Vitrina - Consultando Wasi para ID: {}", inmuebleId);
                WasiInmuebleDTO inmuebleRaw = wasiClient.obtenerInmueblePorId(inmuebleId);

                if (inmuebleRaw != null) {
                    return mapperService.mapToVitrina(inmuebleRaw, estadoDelInmueble, url);
                }
            }
        } catch (Exception e) {
            /*
             * Tolerancia a fallos: Si un inmueble falla, se retorna null para que el filtro
             * lo descarte
             * sin romper la vitrina entera del adulto mayor.
             */
            log.error("Error obteniendo inmueble {}: {}", inmuebleId, e.getMessage());
        }

        return null; // Retorno por defecto si algo falla o no pasa las validaciones
    }

    public InmuebleDetalleDTO obtenerInmuebleEspecifico(String usuarioToken, String parametroRecibido) {

        String inmuebleId = mapperService.extraerIdDeUrl(parametroRecibido);

        if (inmuebleId == null) {
            log.error("No se pudo extraer un ID válido del parámetro recibido: {}", LogSanitizer.sanitizar(parametroRecibido));
            throw new IllegalArgumentException("ID de inmueble no válido");
        }

        /* 1. Generar dinámicamente las propiedades a pedir a HubSpot */
        StringBuilder propiedadesSolicitadas = new StringBuilder(HubSpotConstants.FIRSTNAME);
        for (int i = 1; i <= MAX_VITRINA_SIZE; i++) {
            // Pedimos el listing normal (por retrocompatibilidad)
            propiedadesSolicitadas.append(",").append(HubSpotConstants.LISTING_PREFIX).append(i);
            // Pedimos el listing de Alquiler
            propiedadesSolicitadas.append(",").append(HubSpotConstants.LISTING_PREFIX).append(i).append("_a");
            // Pedimos el listing de Venta
            propiedadesSolicitadas.append(",").append(HubSpotConstants.LISTING_PREFIX).append(i).append("_v");
        }

        /* Obtener contacto de HubSpot */
        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken, propiedadesSolicitadas.toString());
        String estadoActual = "SIN_REVISAR";

        if (contacto != null && contacto.getProperties() != null) {
            /* 2. Extraer mapa dinámico interceptado por @JsonAnySetter */
            java.util.Map<String, String> propiedadesActuales = contacto.getProperties().getPropiedadesDinamicas();

            /* 3. Buscar el inmueble iterando sobre las llaves dinámicas */
            for (int i = 1; i <= MAX_VITRINA_SIZE; i++) {
                String urlEnSlot = propiedadesActuales.get("listing_" + i);

                if (urlEnSlot != null && urlEnSlot.contains(inmuebleId)) { // Comparamos usando el ID limpio
                    estadoActual = mapperService.extraerEstadoDeUrl(urlEnSlot);
                    break; /* Detenemos el bucle en cuanto encontramos su estado */
                }
            }
        }

        /* Lógica de enrutamiento y mapeo a Airtable o Wasi (Intacta) */
        if (inmuebleId.startsWith("rec")) {

            log.info("Enrutando petición de detalle a Airtable para el ID limpio: {}", inmuebleId);
            String tokenFormateado = "Bearer " + airtableToken;

            com.fasterxml.jackson.databind.JsonNode airtableRecord = airtableClient.obtenerRegistro(
                    tokenFormateado, tableName, inmuebleId);

            return mapperService.mapAirtableToDetalle(airtableRecord, estadoActual);

        } else {
            // RUTA B: WASI (Inmueble Público)
            log.info("Enrutando petición de detalle a Wasi para el ID limpio: {}", inmuebleId);
            WasiInmuebleDTO inmuebleRaw = wasiClient.obtenerInmueblePorId(inmuebleId);

            if (inmuebleRaw == null) {
                throw new RecursoNoEncontradoException("El inmueble con ID " + inmuebleId + " no existe en Wasi.");
            }

            return mapperService.mapToDetalle(inmuebleRaw, estadoActual);
        }
    }

    public void asignarInmuebleAutomaticamente(String usuarioToken, String urlWasi, String tipoInmueble) {
        log.info("Intento de asignar inmueble [{}] para usuario: {}", tipoInmueble, usuarioToken);

        String nuevoId = mapperService.extraerIdDeUrl(urlWasi);
        if (nuevoId == null)
            throw new IllegalArgumentException("URL inválida.");

        boolean esAlquiler = "ALQUILER".equalsIgnoreCase(tipoInmueble);

        String propiedadesSolicitadas = construirPropiedadesPorTipo(esAlquiler);

        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken, propiedadesSolicitadas);
        if (contacto == null || contacto.getProperties() == null) {
            throw new RecursoNoEncontradoException("Usuario no encontrado");
        }

        Map<String, String> propiedadesActuales = contacto.getProperties().getPropiedadesDinamicas();
        String huecoLibre = buscarHuecoLibre(propiedadesActuales, nuevoId, esAlquiler);

        if (huecoLibre == null) {
            throw new IllegalStateException(
                    "La vitrina de " + tipoInmueble + " del cliente está llena (" + MAX_VITRINA_SIZE + ").");
        }

        log.info("Asignando inmueble {} en el hueco disponible: {}", nuevoId, huecoLibre);
        Map<String, String> propiedadesAActualizar = new HashMap<>();
        propiedadesAActualizar.put(huecoLibre, urlWasi);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("properties", propiedadesAActualizar);
        hubSpotClient.actualizarContacto(usuarioToken, requestBody);
    }

    /* Construye el nombre interno del slot según tipo y posición */
    private String construirKeyListingPorTipo(int indice, boolean esAlquiler) {
        if (esAlquiler) {
            return HubSpotConstants.LISTING_PREFIX + indice + "_a";
        }
        return (indice <= 5)
                ? HubSpotConstants.LISTING_PREFIX + indice
                : HubSpotConstants.LISTING_PREFIX + indice + "_v";
    }

    /* Genera la lista de propiedades a solicitar a HubSpot filtradas por tipo */
    private String construirPropiedadesPorTipo(boolean esAlquiler) {
        StringBuilder sb = new StringBuilder(HubSpotConstants.FIRSTNAME);
        for (int i = 1; i <= MAX_VITRINA_SIZE; i++) {
            sb.append(",").append(construirKeyListingPorTipo(i, esAlquiler));
        }
        return sb.toString();
    }

    /*
     * Busca duplicados y retorna el primer hueco libre; lanza excepción si hay
     * duplicado
     */
    private String buscarHuecoLibre(Map<String, String> propiedadesActuales, String nuevoId, boolean esAlquiler) {
        String huecoLibre = null;
        for (int i = 1; i <= MAX_VITRINA_SIZE; i++) {
            String key = construirKeyListingPorTipo(i, esAlquiler);
            String urlEnSlot = propiedadesActuales.get(key);

            if (urlEnSlot != null && !urlEnSlot.trim().isEmpty()) {
                String idExistente = mapperService.extraerIdDeUrl(urlEnSlot);
                if (nuevoId.equals(idExistente)) {
                    throw new IllegalStateException("El inmueble " + nuevoId + " ya está asignado en " + key);
                }
            } else if (huecoLibre == null) {
                huecoLibre = key;
            }
        }
        return huecoLibre;
    }

    public void desasignarInmueble(String usuarioToken, String urlRecibida) {
        log.info("Intento de desasignar inmueble para usuario: {}", usuarioToken);

        String idTarget = mapperService.extraerIdDeUrl(urlRecibida);
        if (idTarget == null)
            throw new IllegalArgumentException("URL inválida.");

        /* Pedir TODAS las propiedades de listing (legacy, _a y _v) */
        StringBuilder propiedadesSolicitadas = new StringBuilder(HubSpotConstants.FIRSTNAME);
        for (int i = 1; i <= MAX_VITRINA_SIZE; i++) {
            propiedadesSolicitadas.append(",").append(HubSpotConstants.LISTING_PREFIX).append(i);
            propiedadesSolicitadas.append(",").append(HubSpotConstants.LISTING_PREFIX).append(i).append("_a");
            propiedadesSolicitadas.append(",").append(HubSpotConstants.LISTING_PREFIX).append(i).append("_v");
        }

        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken, propiedadesSolicitadas.toString());
        if (contacto == null || contacto.getProperties() == null) {
            throw new RecursoNoEncontradoException("Usuario no encontrado");
        }

        Map<String, String> propiedadesActuales = contacto.getProperties().getPropiedadesDinamicas();

        /* Buscar en TODAS las categorías */
        for (Map.Entry<String, String> entry : propiedadesActuales.entrySet()) {
            String key = entry.getKey();
            String urlEnSlot = entry.getValue();

            if (key.startsWith(HubSpotConstants.LISTING_PREFIX)
                    && urlEnSlot != null && !urlEnSlot.trim().isEmpty()) {

                String idEnSlot = mapperService.extraerIdDeUrl(urlEnSlot);
                if (idTarget.equals(idEnSlot)) {
                    log.info("Desasignando inmueble del slot: {}", key);
                    actualizarEnHubSpot(usuarioToken, key, "");
                    return;
                }
            }
        }

        throw new RecursoNoEncontradoException("El inmueble no fue encontrado en la vitrina de este cliente.");
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
            HubSpotOwnerDTO owner = hubSpotClient.obtenerAsesor(ownerId);

            String urlFoto;
            String telefonoContacto;
            String urlMeeting;

            if (ownerId.equals(idN)) {
                urlFoto = fotoN;
                telefonoContacto = telN;
                urlMeeting = meetN;
            } else if (ownerId.equals(idS)) {
                urlFoto = fotoS;
                telefonoContacto = telS;
                urlMeeting = meetS;
            } else if (ownerId.equals(idJ)) {
                urlFoto = fotoJ;
                telefonoContacto = telJ;
                urlMeeting = meetJ;
            } else if (ownerId.equals(idD)) {
                urlFoto = fotoD;
                telefonoContacto = telD;
                urlMeeting = meetD;
            } else if (ownerId.equals(idM)) {
                urlFoto = fotoM;
                telefonoContacto = telM;
                urlMeeting = meetM;
            } else if (ownerId.equals(idA)) {
                urlFoto = fotoA;
                telefonoContacto = telA;
                urlMeeting = meetA;
            }
            else if (ownerId.equals(idJS)){
                urlFoto = fotoJS;
                telefonoContacto = telJS;
                urlMeeting = meetJS;
            }
            else {
                log.warn("OwnerId [{}] no coincide con ningún asesor configurado", ownerId);
                urlFoto = "https://via.placeholder.com/200?text=Asesor";
                telefonoContacto = "No disponible";
                urlMeeting = "https://habitarinmobiliaria.co/contacto";
            }
            return VitrinaResponseDTO.AsesorInfo.builder()
                    .nombreCompleto(
                            owner.getFirstName() + " " + (owner.getLastName() != null ? owner.getLastName() : ""))
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