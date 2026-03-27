package co.habitarinmobiliaria.middleware_service.controller;

import co.habitarinmobiliaria.middleware_service.dtos.AsignarInmuebleDTO;
import co.habitarinmobiliaria.middleware_service.dtos.InmuebleDetalleDTO;
import co.habitarinmobiliaria.middleware_service.dtos.VitrinaResponseDTO;
import co.habitarinmobiliaria.middleware_service.service.OrquestadorService;
import co.habitarinmobiliaria.middleware_service.util.LogSanitizer;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

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
        log.info("Solicitud REST recibida para token: {}", LogSanitizer.sanitizar(usuarioToken));

        VitrinaResponseDTO vitrinaResponse = orquestadorService.procesarVitrina(usuarioToken);

        if (vitrinaResponse.getInmuebles().isEmpty()) {
            log.info("Vitrina vacía para el token: {}", LogSanitizer.sanitizar(usuarioToken));
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(3)).mustRevalidate())
                .body(vitrinaResponse);
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
    @Operation(summary = "Asignar inmueble automáticamente", description = "Recibe una URL y un tipo de inmueble, busca el primer espacio vacío correspondiente en HubSpot y lo guarda.")
    public ResponseEntity<String> asignarInmueble(
            @PathVariable String usuarioToken,
            @RequestBody AsignarInmuebleDTO dto) {

        try {
            // Validar que el tipo de inmueble venga en la petición
            if (dto.getTipoInmueble() == null || dto.getTipoInmueble().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El campo 'tipo_inmueble' es obligatorio (VENTA o ALQUILER).");
            }

            // Pasamos ambos datos al servicio
            orquestadorService.asignarInmuebleAutomaticamente(usuarioToken, dto.getUrl(), dto.getTipoInmueble());
            return ResponseEntity.ok("Inmueble asignado correctamente a la vitrina de " + dto.getTipoInmueble());

        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{usuarioToken}/desasignar")
    @Operation(summary = "Desasignar inmueble", description = "Recibe una URL y busca en todas las categorías de listing para limpiar el slot correspondiente.")
    public ResponseEntity<String> desasignarInmueble(
            @PathVariable String usuarioToken,
            @RequestBody AsignarInmuebleDTO dto) {

        try {
            if (dto.getUrl() == null || dto.getUrl().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La URL del inmueble es obligatoria.");
            }

            orquestadorService.desasignarInmueble(usuarioToken, dto.getUrl());
            return ResponseEntity.ok("Inmueble desasignado correctamente.");

        } catch (IllegalStateException | IllegalArgumentException e) {
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
