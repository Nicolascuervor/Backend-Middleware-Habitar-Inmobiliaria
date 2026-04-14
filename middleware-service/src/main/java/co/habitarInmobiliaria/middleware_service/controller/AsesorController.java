package co.habitarinmobiliaria.middleware_service.controller;

import co.habitarinmobiliaria.middleware_service.dtos.ClientesPaginadosDTO;
import co.habitarinmobiliaria.middleware_service.service.AsesorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/asesores")
@RequiredArgsConstructor
public class AsesorController {

    private final AsesorService asesorService;

    @GetMapping("/mis-clientes")
    @Operation(
            summary = "Obtener clientes paginados",
            description = "Lista contactos de HubSpot asignados al owner. "
                    + "Requiere el HubSpot owner id (el mismo que antes via JWT).")
    public ResponseEntity<ClientesPaginadosDTO> obtenerMisClientes(
            @Parameter(description = "ID del propietario en HubSpot (hubspot_owner_id)", required = true)
            @RequestParam("hubspotOwnerId") String hubspotOwnerId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String after) {

        ClientesPaginadosDTO respuesta = asesorService.obtenerMisClientes(hubspotOwnerId, limit, after);
        return ResponseEntity.ok(respuesta);
    }
}
