package co.habitarinmobiliaria.middleware_service.service;

import co.habitarinmobiliaria.middleware_service.client.HubSpotClient;
import co.habitarinmobiliaria.middleware_service.dtos.ClienteAsesorDTO;
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

    public List<ClienteAsesorDTO> obtenerMisClientes(String authHeader) {
        // 1. Extraemos el token puro quitando la palabra "Bearer "
        String token = authHeader.substring(7);

        // 2. Sacamos el ID de HubSpot que guardamos mágicamente en el token
        String ownerId = jwtService.extraerOwnerId(token);
        log.info("Buscando clientes en HubSpot para el Asesor OwnerId: {}", ownerId);

        // 3. Construimos el filtro para la API de Search de HubSpot
        // Documentación: Buscar donde hubspot_owner_id sea IGUAL (EQ) al ID del asesor
        HubSpotSearchRequestDTO.Filter filtroAsesor = HubSpotSearchRequestDTO.Filter.builder()
                .propertyName("hubspot_owner_id")
                .operator("EQ")
                .value(ownerId)
                .build();

        HubSpotSearchRequestDTO.FilterGroup grupoFiltros = HubSpotSearchRequestDTO.FilterGroup.builder()
                .filters(List.of(filtroAsesor))
                .build();

        HubSpotSearchRequestDTO peticionBusqueda = HubSpotSearchRequestDTO.builder()
                .filterGroups(List.of(grupoFiltros))
                // Le pedimos a HubSpot que nos devuelva el nombre y apellido del cliente
                .properties(List.of("firstname", "lastname"))
                .build();

        // 4. Ejecutamos la búsqueda
        HubSpotSearchResponseDTO respuestaHubSpot = hubSpotClient.buscarContactos(peticionBusqueda);

        // 5. Mapeamos la respuesta cruda al DTO limpio para el frontend
        if (respuestaHubSpot.getResults() == null || respuestaHubSpot.getResults().isEmpty()) {
            log.info("El asesor no tiene clientes asignados actualmente.");
            return List.of(); // Devolvemos lista vacía
        }

        return respuestaHubSpot.getResults().stream().map(contacto -> {
            String nombre = contacto.getProperties().getFirstname();
            String apellido = contacto.getProperties().getLastname();
            String nombreCompleto = (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");

            return ClienteAsesorDTO.builder()
                    .idContacto(contacto.getId())
                    .nombreCompleto(nombreCompleto.trim())
                    .build();
        }).collect(Collectors.toList());
    }
}