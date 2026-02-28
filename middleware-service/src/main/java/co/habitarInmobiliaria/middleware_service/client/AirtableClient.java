package co.habitarinmobiliaria.middleware_service.client;

import co.habitarinmobiliaria.middleware_service.config.AirtableFeignConfig;
import co.habitarinmobiliaria.middleware_service.dtos.AirtableResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import co.habitarinmobiliaria.middleware_service.dtos.AirtableCreateRequestDTO;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "airtable-client", url = "${airtable.url}", configuration = AirtableFeignConfig.class)
public interface AirtableClient {

    @GetMapping("/{nombreTabla}")
    AirtableResponseDTO buscarRegistrosPorFormula(
                                                   @PathVariable("nombreTabla") String nombreTabla,
                                                   @RequestParam("filterByFormula") String formula
    );

    @PostMapping("/{tableName}")
    JsonNode crearRegistro(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("tableName") String tableName,
            @RequestBody AirtableCreateRequestDTO request
    );

    @GetMapping("/{tableName}/{recordId}")
    com.fasterxml.jackson.databind.JsonNode obtenerRegistro(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("tableName") String tableName,
            @PathVariable("recordId") String recordId
    );
}