package co.habitarinmobiliaria.middleware_service.controller;

import co.habitarinmobiliaria.middleware_service.dtos.ClienteAsesorDTO;
import co.habitarinmobiliaria.middleware_service.dtos.ClientesPaginadosDTO;
import co.habitarinmobiliaria.middleware_service.service.AsesorService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asesores")
@RequiredArgsConstructor
public class AsesorController {

    private final AsesorService asesorService;

    @GetMapping("/mis-clientes")
    @Operation(summary = "Obtener clientes paginados")
    public ResponseEntity<ClientesPaginadosDTO> obtenerMisClientes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String after) {

        ClientesPaginadosDTO respuesta = asesorService.obtenerMisClientes(authHeader, limit, after);
        return ResponseEntity.ok(respuesta);
    }
}
