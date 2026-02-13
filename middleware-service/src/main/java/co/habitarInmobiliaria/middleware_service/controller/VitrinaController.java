package co.habitarinmobiliaria.middleware_service.controller;


import co.habitarinmobiliaria.middleware_service.dtos.VitrinaInmuebleDTO;
import co.habitarinmobiliaria.middleware_service.service.OrquestadorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vitrina")
@CrossOrigin(origins = "*") // ⚠️ OJO: Configuración de desarrollo. Ver nota abajo.
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
    public ResponseEntity<List<VitrinaInmuebleDTO>> obtenerVitrina(@PathVariable String usuarioToken) {

        log.info("Solicitud REST recibida para token: {}", usuarioToken);

        // 1. Delegamos la lógica al servicio (El cerebro)
        List<VitrinaInmuebleDTO> vitrina = orquestadorService.procesarVitrina(usuarioToken);

        // 2. Decidimos la respuesta HTTP
        if (vitrina.isEmpty()) {
            // Opción A: Devolver 200 OK con lista vacía (Mejor para el Frontend React/Vue)
            // Opción B: Devolver 204 No Content (Técnicamente correcto, pero a veces complica al front)
            log.info("Vitrina vacía para el token: {}", usuarioToken);
            return ResponseEntity.ok(vitrina);
        }

        return ResponseEntity.ok(vitrina);
    }

    /**
     * Endpoint de salud (Health Check)
     * Útil para saber si el middleware está vivo sin hacer toda la lógica pesada.
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Middleware Inmobiliario Operativo - V1.0");
    }
}
