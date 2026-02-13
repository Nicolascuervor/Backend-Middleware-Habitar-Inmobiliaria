package co.habitarinmobiliaria.middleware_service.client;

import co.habitarinmobiliaria.middleware_service.config.HubSpotFeignConfig;
import co.habitarinmobiliaria.middleware_service.dtos.HubSpotContactDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
}
