package co.habitarinmobiliaria.middleware_service.controller;

import co.habitarinmobiliaria.middleware_service.dtos.AsignarInmuebleDTO;
import co.habitarinmobiliaria.middleware_service.dtos.InmuebleDetalleDTO;
import co.habitarinmobiliaria.middleware_service.dtos.VitrinaInmuebleDTO;
import co.habitarinmobiliaria.middleware_service.dtos.VitrinaResponseDTO;
import co.habitarinmobiliaria.middleware_service.exception.RecursoNoEncontradoException;
import co.habitarinmobiliaria.middleware_service.service.OrquestadorService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vitrina")
@RequiredArgsConstructor
@Slf4j
public class VitrinaController {

    private final OrquestadorService orquestadorService;

    /* Cargar vitrina personalizada del usuario */
    @GetMapping("/{usuarioToken}")
    @Operation(summary = "Obtener vitrina completa", description = "Retorna el perfil del asesor y la lista de inmuebles.")
    public ResponseEntity<VitrinaResponseDTO> obtenerVitrina(@PathVariable String usuarioToken) {
        log.info("Solicitud REST recibida para token: {}", usuarioToken);

        VitrinaResponseDTO vitrinaResponse = orquestadorService.procesarVitrina(usuarioToken);

        if (vitrinaResponse.getInmuebles().isEmpty()) {
            log.info("Vitrina vacía para el token: {}", usuarioToken);
        }

        return ResponseEntity.ok(vitrinaResponse);
    }

    @GetMapping("/{usuarioToken}/inmuebles/{inmuebleId}")
    @Operation(summary = "Obtener detalles completos de un inmueble", description = "Retorna toda la información formateada para la vista detallada del abuelo.")
    public ResponseEntity<InmuebleDetalleDTO> obtenerDetalleInmueble(
            @PathVariable String usuarioToken,
            @PathVariable String inmuebleId) {

        InmuebleDetalleDTO detalle = orquestadorService.obtenerInmuebleEspecifico(usuarioToken, inmuebleId);
        return ResponseEntity.ok(detalle);
    }

    /* Health check del middleware */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Middleware Inmobiliario Operativo - V1.0");
    }

    @PatchMapping("/{usuarioToken}/asignar")
    @Operation(summary = "Asignar inmueble automáticamente", description = "Recibe una URL de Wasi, busca el primer espacio vacío (listing_1...5) en HubSpot y lo guarda. Falla si ya existe o está lleno.")
    public ResponseEntity<String> asignarInmueble(
            @PathVariable String usuarioToken,
            @RequestBody AsignarInmuebleDTO dto) {

        try {
            orquestadorService.asignarInmuebleAutomaticamente(usuarioToken, dto.getUrl());
            return ResponseEntity.ok("Inmueble asignado correctamente a la vitrina.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            /* Error de lógica: vitrina llena o duplicado */
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{usuarioToken}/estado/{accion}")
    public ResponseEntity<String> cambiarEstado(
            @PathVariable String usuarioToken,
            @PathVariable String accion,
            @RequestBody AsignarInmuebleDTO dto) {

        /* Validar URL */
        if (dto.getUrl() == null || dto.getUrl().isEmpty()) {
            return ResponseEntity.badRequest().body("La URL del inmueble es obligatoria");
        }

        orquestadorService.procesarCambioEstado(usuarioToken, dto.getUrl(), accion);

        return ResponseEntity.ok("Estado actualizado a " + accion);
    }
}
