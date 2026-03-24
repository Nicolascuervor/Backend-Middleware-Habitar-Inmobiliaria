package co.habitarinmobiliaria.middleware_service.controller;

import co.habitarinmobiliaria.middleware_service.dtos.CrearInmueblePrivadoDTO;
import co.habitarinmobiliaria.middleware_service.service.InmueblePrivadoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/inmuebles-privados")
@RequiredArgsConstructor
public class InmueblePrivadoController {

    private final InmueblePrivadoService inmueblePrivadoService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> crearInmueblePrivado(
            @RequestPart("datos") String datosJson,
            @RequestPart("imagenes") MultipartFile[] imagenes) {

        try {
            /* Deserializar JSON de metadatos */
            CrearInmueblePrivadoDTO dto = objectMapper.readValue(datosJson, CrearInmueblePrivadoDTO.class);
            log.info("Recibida petición para crear inmueble privado: {}", dto.getTitulo());

            /* Crear inmueble en HubSpot */
            Map<String, String> resultado = inmueblePrivadoService.crearInmueble(dto, imagenes);

            /* Construir respuesta */
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Inmueble privado creado exitosamente");
            respuesta.put("folderId", resultado.get("folderId"));
            respuesta.put("codigoIdentificador", resultado.get("codigoIdentificador"));

            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Error al parsear JSON de metadatos", e);
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error en el formato JSON de los metadatos");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<Map<String, Object>> obtenerInmueblePorCodigo(@PathVariable String codigo) {
        log.info("Recibida petición para obtener inmueble con código: {}", codigo);
        Map<String, Object> respuesta = inmueblePrivadoService.obtenerInmueblePorCodigo(codigo);
        return ResponseEntity.ok(respuesta);
    }
}
