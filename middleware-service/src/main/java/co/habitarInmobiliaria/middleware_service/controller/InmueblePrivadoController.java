package co.habitarinmobiliaria.middleware_service.controller;

import co.habitarinmobiliaria.middleware_service.dtos.CrearInmueblePrivadoDTO;
import co.habitarinmobiliaria.middleware_service.service.InmueblePrivadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/inmuebles-privados")
@RequiredArgsConstructor
public class InmueblePrivadoController {

    private final InmueblePrivadoService inmueblePrivadoService;

    @PostMapping
    public ResponseEntity<Map<String, String>> crearInmueblePrivado(@Valid @RequestBody CrearInmueblePrivadoDTO dto) {
        log.info("Recibida petición para crear inmueble privado: {}", dto.getTitulo());

        String airtableId = inmueblePrivadoService.crearInmueble(dto);

        /* Construir respuesta estructurada */
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Inmueble privado creado exitosamente");
        respuesta.put("idAirtable", airtableId);

        /* 201 CREATED */
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}
