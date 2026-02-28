package co.habitarinmobiliaria.middleware_service.client;

import co.habitarinmobiliaria.middleware_service.config.HubSpotFeignConfig;
import co.habitarinmobiliaria.middleware_service.dtos.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "hubspot-client", url = "https://api.hubapi.com", configuration = HubSpotFeignConfig.class)
public interface HubSpotClient {

        /* Obtener contacto por ID con propiedades específicas */
        @GetMapping("/crm/v3/objects/contacts/{contactId}")
        HubSpotContactDTO obtenerContacto(
                        @PathVariable("contactId") String contactId,
                        @RequestParam("properties") String properties);

        @PatchMapping("/crm/v3/objects/contacts/{contactId}")
        HubSpotContactDTO actualizarContacto(
                        @PathVariable("contactId") String contactId,
                        @RequestBody Object propertiesWrapper);

        @GetMapping("/crm/v3/owners/{ownerId}")
        HubSpotOwnerDTO obtenerAsesor(@PathVariable("ownerId") String ownerId);

        @PostMapping("/crm/v3/objects/contacts/search")
        HubSpotSearchResponseDTO buscarContactos(
                        @RequestBody HubSpotSearchRequestDTO requestBody);

        @GetMapping("/crm/v3/objects/contacts/{contactId}")
        HubSpotContactDTO obtenerDetalleContacto(
                        @PathVariable("contactId") String contactId,
                        @RequestParam("properties") List<String> properties);

        /* Traer todos los contactos de un asesor */
        @GetMapping("/crm/v3/objects/contacts")
        HubSpotResponseDTO buscarContactos(
                        @RequestParam("count") int count,
                        @RequestParam("properties") List<String> properties);

}
