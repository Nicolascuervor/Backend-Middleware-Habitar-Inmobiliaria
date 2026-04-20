package co.habitarinmobiliaria.middleware_service.client;

import co.habitarinmobiliaria.middleware_service.config.AirtableFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import co.habitarinmobiliaria.middleware_service.dtos.airtable.AirtableCreateRequestDTO;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "airtable-client", url = "${airtable.url}", configuration = AirtableFeignConfig.class)
public interface AirtableClient {

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