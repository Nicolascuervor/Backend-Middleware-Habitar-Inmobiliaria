package co.habitarinmobiliaria.middleware_service.service;


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
@RequiredArgsConstructor // Inyección de dependencias por constructor (Lombok)
@Slf4j
public class OrquestadorService {

    private final HubSpotClient hubSpotClient;
    private final WasiClient wasiClient;
    private final InmuebleMapperService mapperService;

    @Value("${asesor.n.id}")
    private String idN;

    @Value("${asesor.n.foto}")
    private String fotoN;

    @Value("${asesor.n.tel}")
    private String telN;

    @Value("${asesor.n.meet}")
    private String meetN;


    // --- ASESOR S ---
    @Value("${asesor.s.id}") private String idS;
    @Value("${asesor.s.foto}") private String fotoS;
    @Value("${asesor.s.tel}") private String telS;
    @Value("${asesor.s.meet}") private String meetS;

    // --- ASESOR J ---
    @Value("${asesor.j.id}") private String idJ;
    @Value("${asesor.j.foto}") private String fotoJ;
    @Value("${asesor.j.tel}") private String telJ;
    @Value("${asesor.j.meet}") private String meetJ;

    // --- ASESOR D ---
    @Value("${asesor.d.id}") private String idD;
    @Value("${asesor.d.foto}") private String fotoD;
    @Value("${asesor.d.tel}") private String telD;
    @Value("${asesor.d.meet}") private String meetD;

    // --- ASESOR M ---
    @Value("${asesor.m.id}") private String idM;
    @Value("${asesor.m.foto}") private String fotoM;
    @Value("${asesor.m.tel}") private String telM;
    @Value("${asesor.m.meet}") private String meetM;


    public VitrinaResponseDTO procesarVitrina(String usuarioToken) { // <-- 1. Cambiamos el tipo de retorno
        log.info("Iniciando orquestación completa para usuario: {}", usuarioToken);

        // 1. Obtener datos de HubSpot
        String propiedadesSolicitadas = "firstname,listing_1,listing_2,listing_3,listing_4,listing_5,hubspot_owner_id";
        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken, propiedadesSolicitadas);

        // Validamos si el contacto existe
        if (contacto == null || contacto.getProperties() == null) {
            log.warn("Usuario no encontrado o sin propiedades: {}", usuarioToken);
            // Retornamos el Wrapper vacío de forma segura
            return VitrinaResponseDTO.builder()
                    .asesor(construirInfoAsesor(null))
                    .inmuebles(new ArrayList<>())
                    .build();
        }

        // 2. Extraer URLs de los listings (ignorando nulos)
        HubSpotContactDTO.PropertiesDTO props = contacto.getProperties();
        List<String> urlsListings = Stream.of(
                props.getListing1(),
                props.getListing2(),
                props.getListing3(),
                props.getListing4(),
                props.getListing5()
        ).filter(Objects::nonNull).collect(Collectors.toList());

        List<VitrinaInmuebleDTO> vitrinaFinal = new ArrayList<>();

        // 3. Iterar y consultar Wasi (Tu lógica intacta y resiliente)
        for (String url : urlsListings) {
            String inmuebleId = mapperService.extraerIdDeUrl(url);

            if (inmuebleId != null) {
                try {
                    WasiInmuebleDTO inmuebleRaw = wasiClient.obtenerInmueblePorId(inmuebleId);
                    if (inmuebleRaw != null) {
                        vitrinaFinal.add(mapperService.mapToVitrina(inmuebleRaw));
                    }
                } catch (Exception e) {
                    log.error("Error obteniendo inmueble {}: {}", inmuebleId, e.getMessage());
                }
            }
        }

        // 4. Construir la información del Asesor
        String ownerId = props.getOwnerId(); // Extraemos el ID del dueño
        VitrinaResponseDTO.AsesorInfo infoAsesor = construirInfoAsesor(ownerId);

        // 5. Empaquetar y retornar el objeto final
        return VitrinaResponseDTO.builder()
                .asesor(infoAsesor)
                .inmuebles(vitrinaFinal)
                .build();
    }


    public VitrinaInmuebleDTO obtenerInmuebleEspecifico(String usuarioToken, String inmuebleIdSolicitado) {
        log.info("Solicitando detalle inmueble {} para usuario {}", inmuebleIdSolicitado, usuarioToken);

        // 1. Consultar HubSpot para verificar permisos (Seguridad)
        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken,
                "firstname,listing_1,listing_2,listing_3,listing_4,listing_5,hs_avatar_filemanager_key");

        if (contacto == null || contacto.getProperties() == null) {
            throw new RecursoNoEncontradoException("Cliente no encontrado en HubSpot");
        }

        // 2. Verificar si el ID solicitado está en los listings del cliente
        HubSpotContactDTO.PropertiesDTO props = contacto.getProperties();
        boolean tienePermiso = Stream.of(
                        props.getListing1(), props.getListing2(), props.getListing3(), props.getListing4(), props.getListing5()
                )
                .filter(Objects::nonNull) // Ignoramos nulos
                .map(url -> mapperService.extraerIdDeUrl(url)) // Extraemos el ID de cada URL de HubSpot
                .filter(Objects::nonNull) // Evitamos NPE si el ID es nulo
                .anyMatch(idEnHubSpot -> idEnHubSpot.equals(inmuebleIdSolicitado)); // ¿Coincide alguno?

        if (!tienePermiso) {
            // Si el cliente no tiene esa casa asignada, bloqueamos el acceso
            log.warn("Intento de acceso no autorizado: Usuario {} intentó ver inmueble {}", usuarioToken, inmuebleIdSolicitado);
            throw new RecursoNoEncontradoException("El inmueble no se encuentra en su vitrina asignada.");
        }

        // 3. Si tiene permiso, vamos a Wasi a buscar el detalle fresco
        try {
            WasiInmuebleDTO inmuebleRaw = wasiClient.obtenerInmueblePorId(inmuebleIdSolicitado);
            if (inmuebleRaw == null) {
                throw new RecursoNoEncontradoException("El inmueble existe en HubSpot pero Wasi no devolvió datos.");
            }
            return mapperService.mapToVitrina(inmuebleRaw);
        } catch (Exception e) {
            log.error("Error consultando Wasi para detalle: {}", e.getMessage());
            throw new ErrorExternoException("Error al obtener el detalle del inmueble desde el inventario.");
        }
    }


    public void asignarInmuebleAutomaticamente(String usuarioToken, String urlWasi) {
        log.info("Intento de asignar inmueble para usuario: {}", usuarioToken);

        // 1. Validación Básica: ¿Es una URL válida con ID?
        String nuevoId = mapperService.extraerIdDeUrl(urlWasi);
        if (nuevoId == null) {
            throw new IllegalArgumentException("La URL proporcionada no es válida o no contiene un ID de Wasi.");
        }

        // 2. Traer estado actual de HubSpot (Slots 1 al 5)
        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken, "listing_1,listing_2,listing_3,listing_4,listing_5");
        if (contacto == null || contacto.getProperties() == null) {
            throw new RecursoNoEncontradoException("Usuario no encontrado en HubSpot");
        }

        HubSpotContactDTO.PropertiesDTO props = contacto.getProperties();

        // Array auxiliar para iterar ordenadamente
        String[] slotsActuales = {
                props.getListing1(), props.getListing2(), props.getListing3(), props.getListing4(), props.getListing5()
        };

        // Nombres técnicos de los campos en HubSpot
        String[] nombresCampos = {"listing_1", "listing_2", "listing_3", "listing_4", "listing_5"};

        int slotDestino = -1; // -1 significa "no encontrado aún"

        // 3. Barrido de Slots (Búsqueda de duplicados y huecos vacíos)
        for (int i = 0; i < 5; i++) {
            String urlEnSlot = slotsActuales[i];

            // A. Chequeo de duplicado
            if (urlEnSlot != null) {
                String idExistente = mapperService.extraerIdDeUrl(urlEnSlot);
                if (nuevoId.equals(idExistente)) {
                    throw new IllegalStateException("El inmueble " + nuevoId + " ya está asignado en el espacio " + (i + 1));
                }
            }

            // B. Encontrar el PRIMER hueco vacío (solo si no hemos encontrado uno antes)
            if (urlEnSlot == null && slotDestino == -1) {
                slotDestino = i;
                // NO hacemos break aquí, porque debemos seguir revisando el resto para asegurar que no haya duplicados más adelante
            }
        }

        // 4. Validación de Vitrina Llena
        if (slotDestino == -1) {
            throw new IllegalStateException("La vitrina del cliente está llena (5/5). Debe descartar un inmueble antes de agregar otro.");
        }

        // 5. Construir el Payload para HubSpot
        String campoDestino = nombresCampos[slotDestino];
        log.info("Asignando inmueble {} en el hueco disponible: {}", nuevoId, campoDestino);

        Map<String, String> propiedadesInternas = new HashMap<>();
        propiedadesInternas.put(campoDestino, urlWasi);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("properties", propiedadesInternas);

        // 6. Ejecutar el PATCH
        hubSpotClient.actualizarContacto(usuarioToken, requestBody);
    }


    public void desasignarInmueble(String usuarioToken, String urlWasi) {
        log.info("Solicitud de desasignar inmueble para usuario: {}", usuarioToken);

        // 1. Validar ID objetivo
        String idTarget = mapperService.extraerIdDeUrl(urlWasi);
        if (idTarget == null) {
            throw new IllegalArgumentException("La URL proporcionada no es válida.");
        }

        // 2. Traer estado actual de HubSpot
        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken, "listing_1,listing_2,listing_3,listing_4,listing_5");
        if (contacto == null || contacto.getProperties() == null) {
            throw new RecursoNoEncontradoException("Usuario no encontrado en HubSpot");
        }

        HubSpotContactDTO.PropertiesDTO props = contacto.getProperties();
        String[] slotsActuales = {
                props.getListing1(), props.getListing2(), props.getListing3(), props.getListing4(), props.getListing5()
        };
        String[] nombresCampos = {"listing_1", "listing_2", "listing_3", "listing_4", "listing_5"};

        String campoABorrar = null;

        // 3. Buscar el inmueble en los slots
        for (int i = 0; i < 5; i++) {
            String urlEnSlot = slotsActuales[i];

            if (urlEnSlot != null) {
                String idEnSlot = mapperService.extraerIdDeUrl(urlEnSlot);

                if (idTarget.equals(idEnSlot)) {
                    campoABorrar = nombresCampos[i];
                    log.info("Inmueble {} encontrado en el slot {}. Procediendo a eliminar.", idTarget, campoABorrar);
                    break; // ¡Encontrado! Dejamos de buscar.
                }
            }
        }

        // 4. Ejecutar borrado (si se encontró)
        if (campoABorrar != null) {
            Map<String, String> propiedadesInternas = new HashMap<>();
            // IMPORTANTE: En HubSpot, enviar una cadena vacía "" borra el contenido del campo.
            propiedadesInternas.put(campoABorrar, "");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("properties", propiedadesInternas);

            hubSpotClient.actualizarContacto(usuarioToken, requestBody);
        } else {
            // Opción A: Lanzar error si no existe (Estricto)
            // throw new RecursoNoEncontradoException("El inmueble no estaba asignado a este usuario.");

            // Opción B: No hacer nada y retornar (Idempotente - Recomendado)
            log.warn("El inmueble {} no estaba en la vitrina del usuario. No se hizo nada.", idTarget);
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

            // Variables que llevaremos al Builder
            String f, t, m;

            // COMPARACIÓN DIRECTA CON LAS VARIABLES INYECTADAS
            if (ownerId.equals(idN)) {
                f = fotoN; t = telN; m = meetN;
            } else if (ownerId.equals(idS)) {
                f = fotoS; t = telS; m = meetS;
            } else if (ownerId.equals(idJ)) {
                f = fotoJ; t = telJ; m = meetJ;
            } else if (ownerId.equals(idD)) {
                f = fotoD; t = telD; m = meetD;
            } else if (ownerId.equals(idM)) {
                f = fotoM; t = telM; m = meetM;
            } else {
                log.warn("OwnerId [{}] no coincide con ningún asesor configurado", ownerId);
                f = "https://via.placeholder.com/200?text=Asesor";
                t = "No disponible";
                m = "https://habitarinmobiliaria.co/contacto";
            }

            return VitrinaResponseDTO.AsesorInfo.builder()
                    .nombreCompleto(owner.getFirstName() + " " + (owner.getLastName() != null ? owner.getLastName() : ""))
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