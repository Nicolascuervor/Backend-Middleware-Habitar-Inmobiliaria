package co.habitarinmobiliaria.middleware_service.client;

import co.habitarinmobiliaria.middleware_service.dtos.SirvTokenRequestDTO;
import co.habitarinmobiliaria.middleware_service.dtos.SirvTokenResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "sirvClient", url = "${sirv.api.base-url}")
public interface SirvClient {

    // 1. Obtener el Token de Seguridad
    @PostMapping("/token")
    SirvTokenResponseDTO obtenerToken(@RequestBody SirvTokenRequestDTO request);

    // 2. Subir el archivo binario
    @PostMapping(value = "/files/upload", consumes = "*/*")
    void subirArchivo(
            @RequestHeader("Authorization") String bearerToken,
            @RequestHeader("Content-Type") String contentType,
            @RequestParam("filename") String filename,
            @RequestBody byte[] fileBytes
    );
}
