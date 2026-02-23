package co.habitarinmobiliaria.middleware_service.controller;

import co.habitarinmobiliaria.middleware_service.dtos.ClienteAsesorDTO;
import co.habitarinmobiliaria.middleware_service.service.AsesorService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asesores")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AsesorController {

    private final AsesorService asesorService;

    @GetMapping("/mis-clientes")
    @Operation(summary = "Obtener clientes del Asesor", description = "Extrae el ID del asesor desde el JWT y busca sus clientes asignados en HubSpot.")
    public ResponseEntity<List<ClienteAsesorDTO>> obtenerMisClientes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        List<ClienteAsesorDTO> clientes = asesorService.obtenerMisClientes(authHeader);
        return ResponseEntity.ok(clientes);
    }
}
