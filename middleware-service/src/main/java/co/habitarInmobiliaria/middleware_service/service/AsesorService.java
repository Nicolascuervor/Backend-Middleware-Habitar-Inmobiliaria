package co.habitarinmobiliaria.middleware_service.service;

import co.habitarinmobiliaria.middleware_service.client.HubSpotClient;
import co.habitarinmobiliaria.middleware_service.constans.HubSpotConstants;
import co.habitarinmobiliaria.middleware_service.dtos.*;
import co.habitarinmobiliaria.middleware_service.dtos.hubspot.HubSpotContactDTO;
import co.habitarinmobiliaria.middleware_service.dtos.hubspot.HubSpotSearchRequestDTO;
import co.habitarinmobiliaria.middleware_service.dtos.hubspot.HubSpotSearchResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsesorService {

    private final HubSpotClient hubSpotClient;

    public ClientesPaginadosDTO obtenerMisClientes(String hubspotOwnerId, int limit, String afterToken) {
        String ownerId = hubspotOwnerId != null ? hubspotOwnerId.trim() : "";
        if (ownerId.isEmpty()) {
            throw new IllegalArgumentException("hubspotOwnerId es obligatorio");
        }
        log.info("Buscando clientes paginados para el Asesor OwnerId: {} | Limit: {}", ownerId, limit);

        /* Generar propiedades solicitadas */
        List<String> propiedadesSolicitadas = List.of(
                HubSpotConstants.FIRSTNAME,
                HubSpotConstants.LASTNAME,
                HubSpotConstants.LISTINGS_ALQUILER_DATA,
                HubSpotConstants.LISTINGS_VENTA_DATA
        );

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

        List<ClienteAsesorDTO> listaClientes = respuestaHubSpot.getResults().stream()
                .map(this::mapearContactoACliente)
                .toList();

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

    /**
     * Extrae y mapea los datos crudos de HubSpot hacia nuestro DTO de dominio.
     * Centraliza la lógica y mejora la legibilidad.
     */
    private ClienteAsesorDTO mapearContactoACliente(HubSpotContactDTO contacto) {
        HubSpotContactDTO.PropertiesDTO props = contacto.getProperties();

        String nombre = props.getFirstname() != null ? props.getFirstname() : "";
        String apellido = props.getLastname() != null ? props.getLastname() : "";

        Map<String, String> listingsMap = new HashMap<>();
        Map<String, String> dinamicas = props.getPropiedadesDinamicas();

        if (dinamicas != null) {
            String alquilerJson = dinamicas.get(HubSpotConstants.LISTINGS_ALQUILER_DATA);
            String ventaJson = dinamicas.get(HubSpotConstants.LISTINGS_VENTA_DATA);

            if (alquilerJson != null && !alquilerJson.isBlank())
                listingsMap.put(HubSpotConstants.LISTINGS_ALQUILER_DATA, alquilerJson);
            if (ventaJson != null && !ventaJson.isBlank())
                listingsMap.put(HubSpotConstants.LISTINGS_VENTA_DATA, ventaJson);
        }

        return ClienteAsesorDTO.builder()
                .idContacto(contacto.getId())
                .nombreCompleto((nombre + " " + apellido).trim())
                .listings(listingsMap)
                .build();
    }

}