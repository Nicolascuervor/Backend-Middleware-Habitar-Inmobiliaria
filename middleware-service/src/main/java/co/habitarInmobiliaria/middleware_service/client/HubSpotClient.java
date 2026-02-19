package co.habitarinmobiliaria.middleware_service.client;

import co.habitarinmobiliaria.middleware_service.config.HubSpotFeignConfig;
import co.habitarinmobiliaria.middleware_service.dtos.HubSpotContactDTO;
import co.habitarinmobiliaria.middleware_service.dtos.HubSpotOwnerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "hubspot-client", url = "https://api.hubapi.com", configuration = HubSpotFeignConfig.class)
public interface HubSpotClient {

    /**
     * Obtiene un contacto por su ID (UUID/Token) y solicita propiedades específicas.
     * properties: Lista separada por comas de los campos internos de HubSpot (ej: "firstname,listing_1,listing_2")
     */
    @GetMapping("/crm/v3/objects/contacts/{contactId}")
    HubSpotContactDTO obtenerContacto(
            @PathVariable("contactId") String contactId,
            @RequestParam("properties") String properties
    );

    @PatchMapping("/crm/v3/objects/contacts/{contactId}")
    HubSpotContactDTO actualizarContacto(
            @PathVariable("contactId") String contactId,
            @RequestBody Object propertiesWrapper // Enviaremos un Map anidado
    );

    @GetMapping("/crm/v3/owners/{ownerId}")
    HubSpotOwnerDTO obtenerAsesor(@PathVariable("ownerId") String ownerId);
}
