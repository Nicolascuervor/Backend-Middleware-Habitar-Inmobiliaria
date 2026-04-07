package co.habitarinmobiliaria.middleware_service.controller;

import co.habitarinmobiliaria.middleware_service.dtos.HistoricoInmublesDTO;
import co.habitarinmobiliaria.middleware_service.entities.HistoricoInmubles;
import co.habitarinmobiliaria.middleware_service.service.HistoricoInmublesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/historico-inmuebles")
@RequiredArgsConstructor
@Tag(name = "Histórico Inmuebles", description = "Gestión de registros históricos de inmuebles")
public class HistoricoInmublesController {

    private final HistoricoInmublesService historicoInmublesService;

    @PostMapping
    @Operation(summary = "Guardar un registro histórico de inmueble")
    public ResponseEntity<HistoricoInmubles> guardar(
            @Valid @RequestBody HistoricoInmublesDTO dto) {
        HistoricoInmubles saved = historicoInmublesService.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/lote")
    @Operation(summary = "Guardar un lote de registros históricos de inmuebles")
    public ResponseEntity<List<HistoricoInmubles>> guardarLote(
            @Valid @RequestBody List<HistoricoInmublesDTO> dtos) {
        List<HistoricoInmubles> saved = historicoInmublesService.guardarLote(dtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    @Operation(summary = "Obtener todos los registros históricos")
    public ResponseEntity<List<HistoricoInmubles>> obtenerTodos() {
        return ResponseEntity.ok(historicoInmublesService.obtenerTodos());
    }

    @GetMapping("/por-cliente/{clienteAsociado}")
    @Operation(summary = "Obtener registros históricos por cliente asociado")
    public ResponseEntity<List<HistoricoInmubles>> obtenerPorClienteAsociado(
            @PathVariable Long clienteAsociado) {
        return ResponseEntity.ok(historicoInmublesService.obtenerPorClienteAsociado(clienteAsociado));
    }
}
