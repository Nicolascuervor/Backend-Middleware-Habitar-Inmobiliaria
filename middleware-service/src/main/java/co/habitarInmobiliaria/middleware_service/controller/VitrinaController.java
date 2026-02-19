package co.habitarinmobiliaria.middleware_service.controller;


import co.habitarinmobiliaria.middleware_service.dtos.AsignarInmuebleDTO;
import co.habitarinmobiliaria.middleware_service.dtos.VitrinaInmuebleDTO;
import co.habitarinmobiliaria.middleware_service.dtos.VitrinaResponseDTO;
import co.habitarinmobiliaria.middleware_service.service.OrquestadorService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vitrina")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class VitrinaController {

    private final OrquestadorService orquestadorService;

    /**
     * Endpoint principal para cargar la vitrina personalizada.
     * * @param usuarioToken El UUID o Token que identifica al adulto mayor en HubSpot.
     * @return Lista de inmuebles simplificados (DTOs).
     */
    @GetMapping("/{usuarioToken}")
    @Operation(summary = "Obtener vitrina completa", description = "Retorna el perfil del asesor y la lista de inmuebles.")
    public ResponseEntity<VitrinaResponseDTO> obtenerVitrina(@PathVariable String usuarioToken) {
        log.info("Solicitud REST recibida para token: {}", usuarioToken);

        // 1. Delegamos la lógica al servicio.
        // ¡OJO! Ahora recibimos el Wrapper completo, no una lista.
        VitrinaResponseDTO vitrinaResponse = orquestadorService.procesarVitrina(usuarioToken);

        // 2. Verificamos si la lista interna de inmuebles está vacía para el log
        if (vitrinaResponse.getInmuebles().isEmpty()) {
            log.info("Vitrina vacía para el token: {}", usuarioToken);
        }

        // 3. Devolvemos el 200 OK con nuestro objeto perfectamente empaquetado
        return ResponseEntity.ok(vitrinaResponse);
    }

    @GetMapping("/{usuarioToken}/{inmuebleId}")
    @Operation(summary = "Obtener detalle de inmueble", description = "Retorna el detalle de una vivienda específica, validando que pertenezca al usuario.")
    public ResponseEntity<VitrinaInmuebleDTO> obtenerDetalleInmueble(
            @PathVariable String usuarioToken,
            @PathVariable String inmuebleId) {

        VitrinaInmuebleDTO detalle = orquestadorService.obtenerInmuebleEspecifico(usuarioToken, inmuebleId);
        return ResponseEntity.ok(detalle);
    }

    /**
     * Endpoint de salud (Health Check)
     * Útil para saber si el middleware está vivo sin hacer toda la lógica pesada.
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Middleware Inmobiliario Operativo - V1.0");
    }



    @PatchMapping("/{usuarioToken}/asignar")
    @Operation(summary = "Asignar inmueble automáticamente",
            description = "Recibe una URL de Wasi, busca el primer espacio vacío (listing_1...5) en HubSpot y lo guarda. Falla si ya existe o está lleno.")
    public ResponseEntity<String> asignarInmueble(
            @PathVariable String usuarioToken,
            @RequestBody AsignarInmuebleDTO dto) {

        try {
            orquestadorService.asignarInmuebleAutomaticamente(usuarioToken, dto.getUrlWasi());
            return ResponseEntity.ok("Inmueble asignado correctamente a la vitrina.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Capturamos errores de lógica (Vitrina llena o Duplicado) para devolver Bad Request (400)
            // Nota: Podríamos mover esto al GlobalExceptionHandler si quisiéramos ser más puristas
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PatchMapping("/{usuarioToken}/desasignar")
    @Operation(summary = "Desvincular inmueble",
            description = "Busca un inmueble por su URL/ID en la vitrina del cliente y lo elimina (libera el espacio).")
    public ResponseEntity<String> desasignarInmueble(
            @PathVariable String usuarioToken,
            @RequestBody AsignarInmuebleDTO dto) {

        try {
            orquestadorService.desasignarInmueble(usuarioToken, dto.getUrlWasi());
            return ResponseEntity.ok("Inmueble desvinculado correctamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
