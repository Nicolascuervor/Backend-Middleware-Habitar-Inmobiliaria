package co.habitarinmobiliaria.middleware_service.service;

import co.habitarinmobiliaria.middleware_service.client.HubSpotClient;
import co.habitarinmobiliaria.middleware_service.dtos.ClienteAsesorDTO;
import co.habitarinmobiliaria.middleware_service.dtos.ClientesPaginadosDTO;
import co.habitarinmobiliaria.middleware_service.dtos.HubSpotSearchRequestDTO;
import co.habitarinmobiliaria.middleware_service.dtos.HubSpotSearchResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsesorService {

    private final HubSpotClient hubSpotClient;
    private final JwtService jwtService;

    public ClientesPaginadosDTO obtenerMisClientes(String authHeader, int limit, String afterToken) {
        /* Extraer token y ID del asesor */
        String token = authHeader.substring(7);
        String ownerId = jwtService.extraerOwnerId(token);
        log.info("Buscando clientes paginados para el Asesor OwnerId: {} | Limit: {}", ownerId, limit);

        /* Generar propiedades solicitadas */
        List<String> propiedadesSolicitadas = new java.util.ArrayList<>(List.of("firstname", "lastname"));
        int maxListings = 10;
        for (int i = 1; i <= maxListings; i++) {
            propiedadesSolicitadas.add("listing_" + i);
        }

        /* Construir filtro por asesor */
        HubSpotSearchRequestDTO.Filter filtroAsesor = HubSpotSearchRequestDTO.Filter.builder()
                .propertyName("hubspot_owner_id")
                .operator("EQ")
                .value(ownerId)
                .build();

        /* Armar petición con paginación */
        HubSpotSearchRequestDTO peticionBusqueda = HubSpotSearchRequestDTO.builder()
                .filterGroups(
                        List.of(HubSpotSearchRequestDTO.FilterGroup.builder().filters(List.of(filtroAsesor)).build()))
                .properties(propiedadesSolicitadas)
                .limit(limit)
                .after(afterToken)
                .build();

        /* Llamar a HubSpot */
        HubSpotSearchResponseDTO respuestaHubSpot = hubSpotClient.buscarContactos(peticionBusqueda);

        /* Validar respuesta vacía */
        if (respuestaHubSpot.getResults() == null || respuestaHubSpot.getResults().isEmpty()) {
            log.info("No se encontraron más clientes para el Asesor: {}", ownerId);
            return ClientesPaginadosDTO.builder()
                    .clientes(List.of())
                    .totalClientes(0)
                    .build();
        }

        /* Mapear contactos a DTOs */
        List<ClienteAsesorDTO> listaClientes = respuestaHubSpot.getResults().stream().map(contacto -> {
            co.habitarinmobiliaria.middleware_service.dtos.HubSpotContactDTO.PropertiesDTO props = contacto
                    .getProperties();

            String nombre = props.getFirstname();
            String apellido = props.getLastname();
            String nombreCompleto = (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");

            /* Recolectar listings fijos */
            java.util.Map<String, String> listingsMap = new java.util.HashMap<>();
            if (props.getListing1() != null && !props.getListing1().isEmpty())
                listingsMap.put("listing_1", props.getListing1());
            if (props.getListing2() != null && !props.getListing2().isEmpty())
                listingsMap.put("listing_2", props.getListing2());
            if (props.getListing3() != null && !props.getListing3().isEmpty())
                listingsMap.put("listing_3", props.getListing3());
            if (props.getListing4() != null && !props.getListing4().isEmpty())
                listingsMap.put("listing_4", props.getListing4());
            if (props.getListing5() != null && !props.getListing5().isEmpty())
                listingsMap.put("listing_5", props.getListing5());

            /* Agregar listings dinámicos */
            if (props.getPropiedadesDinamicas() != null) {
                props.getPropiedadesDinamicas().forEach((key, value) -> {
                    if (key.startsWith("listing_") && value != null && !value.isEmpty()) {
                        listingsMap.put(key, value);
                    }
                });
            }

            return ClienteAsesorDTO.builder()
                    .idContacto(contacto.getId())
                    .nombreCompleto(nombreCompleto.trim())
                    .listings(listingsMap)
                    .build();
        }).collect(java.util.stream.Collectors.toList());

        /* Extraer token de siguiente página */
        String siguienteToken = null;
        if (respuestaHubSpot.getPaging() != null && respuestaHubSpot.getPaging().getNext() != null) {
            siguienteToken = respuestaHubSpot.getPaging().getNext().getAfter();
        }

        /* Retornar resultado paginado */
        return ClientesPaginadosDTO.builder()
                .clientes(listaClientes)
                .nextToken(siguienteToken)
                .totalClientes(respuestaHubSpot.getTotal())
                .build();
    }
}