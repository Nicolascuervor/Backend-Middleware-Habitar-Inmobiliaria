package co.habitarinmobiliaria.middleware_service.client;

import co.habitarinmobiliaria.middleware_service.config.AirtableFeignConfig;
import co.habitarinmobiliaria.middleware_service.dtos.AirtableResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "airtable-client", url = "${airtable.url}", configuration = AirtableFeignConfig.class)
public interface AirtableClient {

    @GetMapping("/{nombreTabla}")
    AirtableResponseDTO buscarRegistrosPorFormula( // <-- Cambiamos String por AirtableResponseDTO
                                                   @PathVariable("nombreTabla") String nombreTabla,
                                                   @RequestParam("filterByFormula") String formula
    );
}