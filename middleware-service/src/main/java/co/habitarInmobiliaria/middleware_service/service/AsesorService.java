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
        // 1. Extraer el token y el ID del Asesor (Seguridad)
        String token = authHeader.substring(7);
        String ownerId = jwtService.extraerOwnerId(token);
        log.info("Buscando clientes paginados para el Asesor OwnerId: {} | Limit: {}", ownerId, limit);

        // 2. Generar propiedades solicitadas dinámicamente (Escalabilidad)
        List<String> propiedadesSolicitadas = new java.util.ArrayList<>(List.of("firstname", "lastname"));
        int maxListings = 10;
        for (int i = 1; i <= maxListings; i++) {
            propiedadesSolicitadas.add("listing_" + i);
        }

        // 3. Construir el filtro para buscar por Asesor
        HubSpotSearchRequestDTO.Filter filtroAsesor = HubSpotSearchRequestDTO.Filter.builder()
                .propertyName("hubspot_owner_id")
                .operator("EQ")
                .value(ownerId)
                .build();

        // 4. Armar la petición de búsqueda con PAGINACIÓN
        HubSpotSearchRequestDTO peticionBusqueda = HubSpotSearchRequestDTO.builder()
                .filterGroups(List.of(HubSpotSearchRequestDTO.FilterGroup.builder().filters(List.of(filtroAsesor)).build()))
                .properties(propiedadesSolicitadas)
                .limit(limit) // <-- Controlamos el volumen de datos a traer
                .after(afterToken) // <-- Le indicamos a HubSpot desde qué punto continuar
                .build();

        // 5. Ejecutar la llamada a HubSpot
        HubSpotSearchResponseDTO respuestaHubSpot = hubSpotClient.buscarContactos(peticionBusqueda);

        // 6. Validación de respuesta vacía (Manejo de borde)
        if (respuestaHubSpot.getResults() == null || respuestaHubSpot.getResults().isEmpty()) {
            log.info("No se encontraron más clientes para el Asesor: {}", ownerId);
            return ClientesPaginadosDTO.builder()
                    .clientes(List.of())
                    .totalClientes(0)
                    .build();
        }

        // 7. Mapeo de Contactos a DTOs (Filtrando los vacíos para ahorrar payload)
        List<ClienteAsesorDTO> listaClientes = respuestaHubSpot.getResults().stream().map(contacto -> {
            co.habitarinmobiliaria.middleware_service.dtos.HubSpotContactDTO.PropertiesDTO props = contacto.getProperties();

            String nombre = props.getFirstname();
            String apellido = props.getLastname();
            String nombreCompleto = (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");

            // Recolectamos los listings fijos (1 al 5)
            java.util.Map<String, String> listingsMap = new java.util.HashMap<>();
            if (props.getListing1() != null && !props.getListing1().isEmpty()) listingsMap.put("listing_1", props.getListing1());
            if (props.getListing2() != null && !props.getListing2().isEmpty()) listingsMap.put("listing_2", props.getListing2());
            if (props.getListing3() != null && !props.getListing3().isEmpty()) listingsMap.put("listing_3", props.getListing3());
            if (props.getListing4() != null && !props.getListing4().isEmpty()) listingsMap.put("listing_4", props.getListing4());
            if (props.getListing5() != null && !props.getListing5().isEmpty()) listingsMap.put("listing_5", props.getListing5());

            // Agregamos los listings dinámicos (6 en adelante) interceptados por Jackson
            if(props.getPropiedadesDinamicas() != null) {
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

        // 8. Extraer el token de la siguiente página (Null-safe para evitar NullPointerExceptions)
        String siguienteToken = null;
        if (respuestaHubSpot.getPaging() != null && respuestaHubSpot.getPaging().getNext() != null) {
            siguienteToken = respuestaHubSpot.getPaging().getNext().getAfter();
        }

        // 9. Retornar el objeto paginado final
        return ClientesPaginadosDTO.builder()
                .clientes(listaClientes)
                .nextToken(siguienteToken)
                .totalClientes(respuestaHubSpot.getTotal())
                .build();
    }
}