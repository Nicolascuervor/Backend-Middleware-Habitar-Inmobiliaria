package co.habitarinmobiliaria.middleware_service.service;

import co.habitarinmobiliaria.middleware_service.Strategy.EstadoInmuebleStrategy;
import co.habitarinmobiliaria.middleware_service.client.AirtableClient;
import co.habitarinmobiliaria.middleware_service.client.HubSpotClient;
import co.habitarinmobiliaria.middleware_service.client.WasiClient;
import co.habitarinmobiliaria.middleware_service.dtos.*;
import co.habitarinmobiliaria.middleware_service.exception.ErrorExternoException;
import co.habitarinmobiliaria.middleware_service.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrquestadorService {

    private final List<EstadoInmuebleStrategy> estrategias;
    private final HubSpotClient hubSpotClient;
    private final WasiClient wasiClient;
    private final InmuebleMapperService mapperService;
    private final AirtableClient airtableClient;

    @Value("${airtable.token}")
    private String airtableToken;

    @Value("${airtable.base.id}")
    private String baseId;

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

    public void procesarCambioEstado(String usuarioToken, String urlRecibida, String accion) {
        log.info("Procesando cambio de estado: {} para URL: {}", accion, urlRecibida);

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

        /* Obtener contacto de HubSpot */
        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken,
                "listing_1,listing_2,listing_3,listing_4,listing_5");

        if (contacto == null || contacto.getProperties() == null) {
            throw new RecursoNoEncontradoException("No se encontró el contacto en HubSpot");
        }

        HubSpotContactDTO.PropertiesDTO props = contacto.getProperties();
        String[] slots = { props.getListing1(), props.getListing2(), props.getListing3(), props.getListing4(),
                props.getListing5() };
        String[] nombresCampos = { "listing_1", "listing_2", "listing_3", "listing_4", "listing_5" };

        /* Buscar y actualizar el slot correcto */
        for (int i = 0; i < 5; i++) {
            if (slots[i] != null) {
                String idEnSlot = mapperService.extraerIdDeUrl(slots[i]);

                if (idTarget.equals(idEnSlot)) {

                    /* Quitar estado previo y aplicar nuevo */
                    String urlBase = slots[i].replaceAll("-[A-Z_]+$", "");
                    String urlNueva = urlBase + estrategia.getSufijo();

                    log.info("Actualizando slot {} con nueva URL: {}", nombresCampos[i], urlNueva);
                    actualizarEnHubSpot(usuarioToken, nombresCampos[i], urlNueva);
                    return;
                }
            }
        }
        throw new RecursoNoEncontradoException("El inmueble no pertenece a la vitrina de este cliente");
    }

    private void actualizarEnHubSpot(String token, String campo, String valor) {
        Map<String, String> prop = Collections.singletonMap(campo, valor);
        hubSpotClient.actualizarContacto(token, Collections.singletonMap("properties", prop));
    }

    public VitrinaResponseDTO procesarVitrina(String usuarioToken) {
        log.info("Iniciando orquestación completa para usuario: {}", usuarioToken);

        /* Obtener datos de HubSpot */
        String propiedadesSolicitadas = "firstname,listing_1,listing_2,listing_3,listing_4,listing_5,hubspot_owner_id";
        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken, propiedadesSolicitadas);

        /* Validar contacto */
        if (contacto == null || contacto.getProperties() == null) {
            log.warn("Usuario no encontrado o sin propiedades: {}", usuarioToken);
            return VitrinaResponseDTO.builder()
                    .asesor(construirInfoAsesor(null))
                    .inmuebles(new ArrayList<>())
                    .build();
        }

        /* Extraer URLs de listings */
        HubSpotContactDTO.PropertiesDTO props = contacto.getProperties();
        List<String> urlsListings = Stream.of(
                props.getListing1(),
                props.getListing2(),
                props.getListing3(),
                props.getListing4(),
                props.getListing5()).filter(Objects::nonNull).collect(Collectors.toList());

        List<VitrinaInmuebleDTO> vitrinaFinal = new ArrayList<>();

        /* Iterar y consultar cada inmueble */
        for (String url : urlsListings) {

            String inmuebleId = mapperService.extraerIdDeUrl(url);
            String estadoDelInmueble = mapperService.extraerEstadoDeUrl(url);

            if (inmuebleId != null) {
                try {
                    /* Ruta Airtable o Wasi según el ID */
                    if (inmuebleId.startsWith("rec")) {
                        log.info("Vitrina - Consultando Airtable para ID: {}", inmuebleId);
                        String tokenFormateado = "Bearer " + airtableToken;
                        com.fasterxml.jackson.databind.JsonNode airtableRecord = airtableClient.obtenerRegistro(
                                tokenFormateado, tableName, inmuebleId);

                        if (airtableRecord != null && airtableRecord.has("fields")) {
                            vitrinaFinal
                                    .add(mapperService.mapAirtableToVitrina(airtableRecord, estadoDelInmueble, url));
                        }
                    } else {
                        log.info("Vitrina - Consultando Wasi para ID: {}", inmuebleId);
                        WasiInmuebleDTO inmuebleRaw = wasiClient.obtenerInmueblePorId(inmuebleId);

                        if (inmuebleRaw != null) {
                            vitrinaFinal.add(mapperService.mapToVitrina(inmuebleRaw, estadoDelInmueble, url));
                        }
                    }
                } catch (Exception e) {
                    /* Tolerancia a fallos por inmueble */
                    log.error("Error obteniendo inmueble {}: {}", inmuebleId, e.getMessage());
                }
            }
        }

        /* Construir info del asesor */
        String ownerId = props.getOwnerId();
        VitrinaResponseDTO.AsesorInfo infoAsesor = construirInfoAsesor(ownerId);

        /* Retornar respuesta final */
        return VitrinaResponseDTO.builder()
                .asesor(infoAsesor)
                .inmuebles(vitrinaFinal)
                .build();
    }

    public InmuebleDetalleDTO obtenerInmuebleEspecifico(String usuarioToken, String inmuebleId) {

        /* Obtener estado desde HubSpot */
        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken,
                "listing_1,listing_2,listing_3,listing_4,listing_5");
        String estadoActual = "SIN_REVISAR";

        if (contacto != null && contacto.getProperties() != null) {
            HubSpotContactDTO.PropertiesDTO props = contacto.getProperties();
            String[] slots = { props.getListing1(), props.getListing2(), props.getListing3(), props.getListing4(),
                    props.getListing5() };
            for (String url : slots) {
                if (url != null && url.contains(inmuebleId)) {
                    estadoActual = mapperService.extraerEstadoDeUrl(url);
                    break;
                }
            }
        }

        /* Enrutar a Airtable o Wasi */
        if (inmuebleId != null && inmuebleId.startsWith("rec")) {
            log.info("Enrutando petición a Airtable para el ID: {}", inmuebleId);

            String tokenFormateado = "Bearer " + airtableToken;
            com.fasterxml.jackson.databind.JsonNode airtableRecord = airtableClient.obtenerRegistro(
                    tokenFormateado, tableName, inmuebleId);

            log.info("JSON CRUDO DE AIRTABLE: {}", airtableRecord.toPrettyString());

            return mapperService.mapAirtableToDetalle(airtableRecord, estadoActual);

        } else {
            log.info("Enrutando petición a Wasi para el ID: {}", inmuebleId);

            WasiInmuebleDTO inmuebleRaw = wasiClient.obtenerInmueblePorId(inmuebleId);

            if (inmuebleRaw == null) {
                throw new RecursoNoEncontradoException("El inmueble con ID " + inmuebleId + " no existe en Wasi.");
            }

            return mapperService.mapToDetalle(inmuebleRaw, estadoActual);
        }
    }

    public void asignarInmuebleAutomaticamente(String usuarioToken, String urlWasi) {
        log.info("Intento de asignar inmueble para usuario: {}", usuarioToken);

        /* Validar URL */
        String nuevoId = mapperService.extraerIdDeUrl(urlWasi);
        if (nuevoId == null) {
            throw new IllegalArgumentException("La URL proporcionada no es válida o no contiene un ID de Wasi.");
        }

        /* Traer slots desde HubSpot */
        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken,
                "listing_1,listing_2,listing_3,listing_4,listing_5");
        if (contacto == null || contacto.getProperties() == null) {
            throw new RecursoNoEncontradoException("Usuario no encontrado en HubSpot");
        }

        HubSpotContactDTO.PropertiesDTO props = contacto.getProperties();

        String[] slotsActuales = {
                props.getListing1(), props.getListing2(), props.getListing3(), props.getListing4(), props.getListing5()
        };

        String[] nombresCampos = { "listing_1", "listing_2", "listing_3", "listing_4", "listing_5" };

        int slotDestino = -1;

        /* Buscar duplicados y hueco libre */
        for (int i = 0; i < 5; i++) {
            String urlEnSlot = slotsActuales[i];

            /* Verificar duplicado */
            if (urlEnSlot != null) {
                String idExistente = mapperService.extraerIdDeUrl(urlEnSlot);
                if (nuevoId.equals(idExistente)) {
                    throw new IllegalStateException(
                            "El inmueble " + nuevoId + " ya está asignado en el espacio " + (i + 1));
                }
            }

            /* Guardar primer hueco vacío */
            if (urlEnSlot == null && slotDestino == -1) {
                slotDestino = i;
            }
        }

        /* Vitrina llena */
        if (slotDestino == -1) {
            throw new IllegalStateException(
                    "La vitrina del cliente está llena (5/5). Debe descartar un inmueble antes de agregar otro.");
        }

        /* Actualizar en HubSpot */
        String campoDestino = nombresCampos[slotDestino];
        log.info("Asignando inmueble {} en el hueco disponible: {}", nuevoId, campoDestino);

        Map<String, String> propiedadesInternas = new HashMap<>();
        propiedadesInternas.put(campoDestino, urlWasi);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("properties", propiedadesInternas);

        hubSpotClient.actualizarContacto(usuarioToken, requestBody);
    }

    public void desasignarInmueble(String usuarioToken, String urlWasi) {
        log.info("Solicitud de marcar inmueble como DESCARTADO para usuario: {}", usuarioToken);

        /* Limpiar URL de entrada */
        String urlLimpiaRequest = urlWasi.replace("-DESCARTADO", "").replace("-APROBADO", "");
        String idTarget = mapperService.extraerIdDeUrl(urlLimpiaRequest);

        if (idTarget == null) {
            throw new IllegalArgumentException("La URL proporcionada no es válida.");
        }

        /* Traer slots desde HubSpot */
        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken,
                "listing_1,listing_2,listing_3,listing_4,listing_5");
        if (contacto == null || contacto.getProperties() == null) {
            throw new RecursoNoEncontradoException("Usuario no encontrado en HubSpot");
        }

        HubSpotContactDTO.PropertiesDTO props = contacto.getProperties();
        String[] slotsActuales = {
                props.getListing1(), props.getListing2(), props.getListing3(), props.getListing4(), props.getListing5()
        };
        String[] nombresCampos = { "listing_1", "listing_2", "listing_3", "listing_4", "listing_5" };

        String campoAModificar = null;
        String urlModificada = null;

        /* Buscar inmueble en slots */
        for (int i = 0; i < 5; i++) {
            String urlEnSlot = slotsActuales[i];

            if (urlEnSlot != null) {
                String urlLimpiaSlot = urlEnSlot.replace("-DESCARTADO", "").replace("-APROBADO", "");
                String idEnSlot = mapperService.extraerIdDeUrl(urlLimpiaSlot);

                if (idTarget.equals(idEnSlot)) {
                    campoAModificar = nombresCampos[i];

                    /* Evitar actualización redundante */
                    if (urlEnSlot.endsWith("-DESCARTADO")) {
                        log.warn("El inmueble {} ya estaba descartado. No se harán cambios.", idTarget);
                        return;
                    }

                    urlModificada = urlLimpiaSlot + "-DESCARTADO";
                    log.info("Inmueble {} encontrado en el slot {}. Marcando como DESCARTADO.", idTarget,
                            campoAModificar);
                    break;
                }
            }
        }

        /* Ejecutar actualización */
        if (campoAModificar != null && urlModificada != null) {
            Map<String, String> propiedadesInternas = new HashMap<>();
            propiedadesInternas.put(campoAModificar, urlModificada);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("properties", propiedadesInternas);

            hubSpotClient.actualizarContacto(usuarioToken, requestBody);
            log.info("Actualización exitosa: {} ahora es {}", campoAModificar, urlModificada);
        } else {
            throw new IllegalArgumentException("El inmueble no estaba en la vitrina del usuario.");
        }
    }

    public void aprobarInmueble(String usuarioToken, String urlWasi) {
        log.info("Solicitud de marcar inmueble como APROBADO para usuario: {}", usuarioToken);

        /* Limpiar URL de entrada */
        String urlLimpiaRequest = urlWasi.replace("-DESCARTADO", "").replace("-APROBADO", "");
        String idTarget = mapperService.extraerIdDeUrl(urlLimpiaRequest);

        if (idTarget == null) {
            throw new IllegalArgumentException("La URL proporcionada no es válida.");
        }

        /* Traer slots desde HubSpot */
        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken,
                "listing_1,listing_2,listing_3,listing_4,listing_5");
        if (contacto == null || contacto.getProperties() == null) {
            throw new RecursoNoEncontradoException("Usuario no encontrado en HubSpot");
        }

        HubSpotContactDTO.PropertiesDTO props = contacto.getProperties();
        String[] slotsActuales = {
                props.getListing1(), props.getListing2(), props.getListing3(), props.getListing4(), props.getListing5()
        };
        String[] nombresCampos = { "listing_1", "listing_2", "listing_3", "listing_4", "listing_5" };

        String campoAModificar = null;
        String urlModificada = null;

        /* Buscar inmueble en slots */
        for (int i = 0; i < 5; i++) {
            String urlEnSlot = slotsActuales[i];

            if (urlEnSlot != null) {
                String urlLimpiaSlot = urlEnSlot.replace("-DESCARTADO", "").replace("-APROBADO", "");
                String idEnSlot = mapperService.extraerIdDeUrl(urlLimpiaSlot);

                if (idTarget.equals(idEnSlot)) {
                    campoAModificar = nombresCampos[i];

                    /* Evitar actualización redundante */
                    if (urlEnSlot.endsWith("-APROBADO")) {
                        log.warn("El inmueble {} ya estaba aprobado. No se harán cambios.", idTarget);
                        return;
                    }

                    urlModificada = urlLimpiaSlot + "-APROBADO";
                    log.info("Inmueble {} encontrado en el slot {}. Marcando como APROBADO.", idTarget,
                            campoAModificar);
                    break;
                }
            }
        }

        /* Ejecutar actualización */
        if (campoAModificar != null && urlModificada != null) {
            Map<String, String> propiedadesInternas = new HashMap<>();
            propiedadesInternas.put(campoAModificar, urlModificada);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("properties", propiedadesInternas);

            hubSpotClient.actualizarContacto(usuarioToken, requestBody);
            log.info("Actualización exitosa: {} ahora es {}", campoAModificar, urlModificada);
        } else {
            throw new IllegalArgumentException("El inmueble no estaba en la vitrina del usuario.");
        }
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

            /* Datos del asesor según ID */
            String f, t, m;

            if (ownerId.equals(idN)) {
                f = fotoN;
                t = telN;
                m = meetN;
            } else if (ownerId.equals(idS)) {
                f = fotoS;
                t = telS;
                m = meetS;
            } else if (ownerId.equals(idJ)) {
                f = fotoJ;
                t = telJ;
                m = meetJ;
            } else if (ownerId.equals(idD)) {
                f = fotoD;
                t = telD;
                m = meetD;
            } else if (ownerId.equals(idM)) {
                f = fotoM;
                t = telM;
                m = meetM;
            } else {
                log.warn("OwnerId [{}] no coincide con ningún asesor configurado", ownerId);
                f = "https://via.placeholder.com/200?text=Asesor";
                t = "No disponible";
                m = "https://habitarinmobiliaria.co/contacto";
            }

            return VitrinaResponseDTO.AsesorInfo.builder()
                    .nombreCompleto(
                            owner.getFirstName() + " " + (owner.getLastName() != null ? owner.getLastName() : ""))
                    .correo(owner.getEmail())
                    .telefono(t)
                    .fotoUrl(f)
                    .linkMeeting(m)
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